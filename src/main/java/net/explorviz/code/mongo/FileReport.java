package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  ...
 */
public class FileReport extends PanacheMongoEntity {

  private String landscapeToken;
  private String appName;
  private String commitId;
  private String fileName;
  private String packageName;
  private List<String> importName;
  private Map<String, ClassData2> classData;
  private Map<String, String> fileMetric;
  private String author;
  private String modifiedLines;
  private String addedLines;
  private String deletedLines;

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public String getAppName() {
    return this.appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public List<String> getImportName() {
    return this.importName;
  }

  public void setImportName(List<String> importName) {
    this.importName = importName;
  }

  public Map<String, ClassData2> getClassData() {
    return this.classData;
  }

  public void setClassData(Map<String, ClassData2> classData) {
    this.classData = classData;
  }

  public Map<String, String> getFileMetric() {
    return this.fileMetric;
  }

  public void setFileMetric(Map<String, String> fileMetric) {
    this.fileMetric = fileMetric;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getModifiedLines() {
    return this.modifiedLines;
  }

  public void setModifiedLines(String modifiedLines) {
    this.modifiedLines = modifiedLines;
  }

  public String getAddedLines() {
    return this.addedLines;
  }

  public void setAddedLines(String addedLines) {
    this.addedLines = addedLines;
  }

  public String getDeletedLines() {
    return this.deletedLines;
  }

  public void setDeletedLines(String deletedLines) {
    this.deletedLines = deletedLines;
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
    
    if (!tmpString.equals("")) {
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

  /**
   * ...
   */
  public static class ClassData2 {

    private ClassType2 type;
    private List<String> modifier;
    private List<String> intrfc;
    private List<FieldData2> field;
    private List<String> innerClass;
    private List<MethodData2> constructor;
    private Map<String, MethodData2>  methodData;
    private List<String> variable;
    private String superClass;
    private List<String> enumConstant;
    private List<String> annotation;
    private Map<String, String> classMetric;

    public ClassType2 getType() {
      return this.type;
    }

    public void setType(ClassType2 type) {
      this.type = type;
    }

    public List<String> getModifier() {
      return this.modifier;
    }

    public void setModifier(List<String> modifier) {
      this.modifier = modifier;
    }

    public List<String> getIntrfc() {
      return this.intrfc;
    }

    public void setIntrfc(List<String> intrfc) {
      this.intrfc = intrfc;
    }

    public List<FieldData2> getField() {
      return this.field;
    }

    public void setField(List<FieldData2> field) {
      this.field = field;
    }

    public List<String> getInnerClass() {
      return this.innerClass;
    }

    public void setInnerClass(List<String> innerClass) {
      this.innerClass = innerClass;
    }

    public List<MethodData2> getConstructor() {
      return this.constructor;
    }

    public void setConstructor(List<MethodData2> constructor) {
      this.constructor = constructor;
    }

    public Map<String, MethodData2> getMethodData() {
      return this.methodData;
    }

    public void setMethodData(Map<String, MethodData2> methodData) {
      this.methodData = methodData;
    }

    public List<String> getVariable() {
      return this.variable;
    }

    public void setVariable(List<String> variable) {
      this.variable = variable;
    }

    public String getSuperClass() {
      return this.superClass;
    }

    public void setSuperClass(String superClass) {
      this.superClass = superClass;
    }

    public List<String> getEnumConstant() {
      return this.enumConstant;
    }

    public void setEnumConstant(List<String> enumConstant) {
      this.enumConstant = enumConstant;
    }

    public List<String> getAnnotation() {
      return this.annotation;
    }

    public void setAnnotation(List<String> annotation) {
      this.annotation = annotation;
    }

    public Map<String, String> getClassMetric() {
      return this.classMetric;
    }

    public void setClassMetric(Map<String, String> classMetric) {
      this.classMetric = classMetric;
    }


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

      private String name;
      private String type;
      private List<String> modifier;

      public String getName() {
        return this.name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public String getType() {
        return this.type;
      }

      public void setType(String type) {
        this.type = type;
      }

      public List<String> getModifier() {
        return this.modifier;
      }

      public void setModifier(List<String> modifier) {
        this.modifier = modifier;
      }
    }

    /**
     * ...
     */
    public static class MethodData2 {

      private String returnType;
      private List<String> modifier;
      private List<ParameterData2> parameter;
      private List<String> outgoingMethodCalls;
      private boolean isConstructor;
      private List<String> annotation;
      private Map<String, String> metric;

      public String getReturnType() {
        return this.returnType;
      }

      public void setReturnType(String returnType) {
        this.returnType = returnType;
      }

      public List<String> getModifier() {
        return this.modifier;
      }

      public void setModifier(List<String> modifier) {
        this.modifier = modifier;
      }

      public List<ParameterData2> getParameter() {
        return this.parameter;
      }

      public void setParameter(List<ParameterData2> parameter) {
        this.parameter = parameter;
      }

      public List<String> getOutgoingMethodCalls() {
        return this.outgoingMethodCalls;
      }

      public void setOutgoingMethodCalls(List<String> outgoingMethodCalls) {
        this.outgoingMethodCalls = outgoingMethodCalls;
      }

      public boolean isIsConstructor() {
        return this.isConstructor;
      }

      public boolean getIsConstructor() {
        return this.isConstructor;
      }

      public void setIsConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
      }

      public List<String> getAnnotation() {
        return this.annotation;
      }

      public void setAnnotation(List<String> annotation) {
        this.annotation = annotation;
      }

      public Map<String, String> getMetric() {
        return this.metric;
      }

      public void setMetric(Map<String, String> metric) {
        this.metric = metric;
      }

      /**
       * ...
       */
      public static class ParameterData2 {

        private String name;
        private String type;
        private List<String> modifier;

        public String getName() {
          return this.name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getType() {
          return this.type;
        }

        public void setType(String type) {
          this.type = type;
        }

        public List<String> getModifier() {
          return this.modifier;
        }

        public void setModifier(List<String> modifier) {
          this.modifier = modifier;
        }
      }
    }
  }
}


