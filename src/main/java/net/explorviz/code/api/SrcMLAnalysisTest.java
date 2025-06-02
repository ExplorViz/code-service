package net.explorviz.code.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.t;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.explorviz.code.analysis.XMLFromSrcMLAnalysis;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.dto.LandscapeStructure;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package.Class;

@Path("/v2/code/")
public class SrcMLAnalysisTest {

  private final XMLFromSrcMLAnalysis xmlFromSrcMLAnalysis;
  private final LandscapeStructureHelper landscapeStructureHelper;

  @Inject
  public SrcMLAnalysisTest(final XMLFromSrcMLAnalysis xmlFromSrcMLAnalysis, 
      final LandscapeStructureHelper landscapeStructureHelper) {
    this.xmlFromSrcMLAnalysis = xmlFromSrcMLAnalysis;
    this.landscapeStructureHelper = landscapeStructureHelper;
  }

  @GET
  @Path("srcML")
  public LandscapeStructure test() {
    InputStream xmlStream = XMLFromSrcMLAnalysis.class.getResourceAsStream("/crypto.xml");
    NodeList nodeList = this.xmlFromSrcMLAnalysis.retrieveNodeListFromXMLFile(xmlStream);

    final Map<String, Package> packageNameToPackageMap =
        new HashMap<>();

    if(nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);

        if(Node.ELEMENT_NODE == nodeList.item(i).getNodeType()) {
          Element unitElement = (Element) node;

          if (unitElement.hasAttribute("filename")) {
            String filename = unitElement.getAttribute("filename");
            String[] parts = filename.split("/");
            String key = "";
            String parentKey = "";
            for(int j = 0; j < parts.length - 1; j++) {
              parentKey = key;
              key += parts[j] + "/";
              Package currentPackage = packageNameToPackageMap.get(key);
              if(currentPackage == null) {
                final Package newPackage = new Package();
                newPackage.setName(parts[j]);
                currentPackage = newPackage;

                if(j > 0) {
                  // update parent package if necessary
                  Package parentPackage = packageNameToPackageMap.get(parentKey);
                  boolean exists = false;
                  for (Package subPkg : parentPackage.getSubPackages()) {
                    if (subPkg.getName().equals(parts[j])) {
                      exists = true;
                      break;
                    }
                  }
                  if (!exists) {
                    parentPackage.getSubPackages().add(currentPackage);
                  }
                }
                packageNameToPackageMap.put(key, currentPackage);
              }
            }

            if("".equals(key)) {
              // If the key is empty, it means there is no package name
              continue; // No package name, skip
            }
            Package pkg = packageNameToPackageMap.get(key);
            NodeList functions = unitElement.getElementsByTagName("function");
            List<Method> methods = new ArrayList<>();
            for (int j = 0; j < functions.getLength(); j++) {
                Node functionNode = functions.item(j);
                NodeList children = functionNode.getChildNodes();

                if (functionNode.getNodeType() == Node.ELEMENT_NODE) {

                    for (int k = 0; k < children.getLength(); k++) {
                        Node child = children.item(k);
                        // We want: a <name> element that's a direct child of <function>
                        if (child.getNodeType() == Node.ELEMENT_NODE &&
                            "name".equals(child.getLocalName())) {
                            Method method = new Method();
                            method.setName(child.getTextContent().trim());
                            methods.add(method);
                        }
                    }
                }
            }
            
            Class clazz = new Class(parts[parts.length - 1]);
            clazz.setMethods(methods);
            pkg.getClasses().add(clazz);
          }
        }
        
      }
    }


    List<Package> topLevelPackages = new ArrayList<>();
    for (Map.Entry<String, Package> entry : packageNameToPackageMap.entrySet()) {
        String key = entry.getKey();
        // A top-level package has exactly one '/', which is at the end of the key 
        if (!key.isEmpty() && key.indexOf('/', 0) == key.length() - 1) {
            topLevelPackages.add(entry.getValue());
        }
    }

    return this.landscapeStructureHelper.buildLandscapeStructure("token", "appname", topLevelPackages);
  }
}




