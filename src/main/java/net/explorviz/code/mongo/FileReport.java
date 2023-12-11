package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  ...
 */
public class FileReport extends PanacheMongoEntity {

  public String landscapeToken;
  public String appName;
  public String commitId;
  public String fileName;
  public String packageName;
  public List<String> importName;
  public Map<String, ClassData2> classData;
  public Map<String, String> fileMetric;
  public String author;
  public String modifiedLines;
  public String addedLines;
  public String deletedLines;

  /**
   * ...
   */
  public static class ClassData2 {
    public ClassType2 type;
    public List<String> modifier;
    public List<String> intrfc;
    public List<FieldData2> field;
    public List<String> innerClass;
    public List<MethodData2> constructor;
    public Map<String, MethodData2>  methodData;
    public List<String> variable;
    public String superClass;
    public List<String> enumConstant;
    public List<String> annotation;
    public Map<String, String> classMetric;

    /**
     * ...
     */
    public enum ClassType2 {
        INTERFACE,
        ABSTRACT_CLASS,
        CLASS,
        ENUM,
        ANONYMOUS_CLASS;
    }

    /**
     * ...
     */
    public static class FieldData2 {
      public String name;
      public String type;
      public List<String> modifier;
    }

    /**
     * ...
     */
    public static class MethodData2 {
      public String returnType;
      public List<String> modifier;
      public List<ParameterData2> parameter;
      public List<String> outgoingMethodCalls;
      public boolean isConstructor;
      public List<String> annotation;
      public Map<String, String> metric;

      /**
       * ...
       */
      public static class ParameterData2 {
        public String name;
        public String type;
        public List<String> modifier;
      }
    }
  }

  /**
   * ...
   ** @param landscapeToken ...
   ** @param appName ...
   ** @param fqFileName ...
   ** @param commitId ...
   ** @return ...
   */
  public static FileReport findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
      final String landscapeToken, 
      final String appName, 
      final String fqFileName, final String commitId) {
    final String[] temp = fqFileName.split("\\.");
    if (temp.length < 2) {
      return null;
    }
    final String fileName = temp[temp.length - 2] + "." + temp[temp.length - 1];
    String tmpString = "";
    for (int i = 0; i < temp.length - 2; i++) {
      tmpString += temp[i];
      tmpString += ".";
    } 
    
    if (!tmpString.equals("")){
      tmpString = tmpString.substring(0, tmpString.length() - 1);
    } 
    
    final String folders = tmpString;
    List<FileReport> fileReportList = find(
        "landscapeToken = ?1 and appName = ?2 and fileName =?3 and commitId =?4", 
        landscapeToken, appName, fileName, commitId).list();

    List<FileReport> filterFileReportList = fileReportList.stream()
        .filter(fr -> folders.endsWith(fr.packageName)).collect(Collectors.toList());
    
    if (filterFileReportList.size() == 1) {
      return filterFileReportList.get(0);
    } else {
      return null;
    }
  }

  public static List<FileReport> findByTokenAndAppNameAndFileName(
      final String landscapeToken, 
      final String appName, final String fileName) {
    return find("landscapeToken = ?1 and appName = ?2 and fileName =?3", 
        landscapeToken, appName, fileName).list();
  }
}


