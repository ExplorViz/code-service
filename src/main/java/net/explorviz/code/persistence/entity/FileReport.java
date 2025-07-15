package net.explorviz.code.persistence.entity; // NOPMD

import java.util.List;
import java.util.Map;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

/**
 * A class for the file reports that the code-agent sends to us.
 */
public class FileReport {

  @BsonId
  private ObjectId id;

  private String landscapeToken;
  private String appName;
  private String commitId;
  private String fileName; // class name (not full qualified) + ".java"
  private String packageName; // includes parent package names separated by dots
  private List<String> importName; // list of full qualified class names
  private Map<String, ClassData2> classData;
  private Map<String, String> fileMetric;
  private String author;
  private String modifiedLines;
  private String addedLines;
  private String deletedLines;

  public ObjectId getId() {
    return this.id;
  }

  public void setId(final ObjectId id) {
    this.id = id;
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public String getAppName() {
    return this.appName;
  }

  public void setAppName(final String appName) {
    this.appName = appName;
  }

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(final String commitId) {
    this.commitId = commitId;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public void setPackageName(final String packageName) {
    this.packageName = packageName;
  }

  public List<String> getImportName() {
    return this.importName;
  }

  public void setImportName(final List<String> importName) {
    this.importName = importName;
  }

  public Map<String, ClassData2> getClassData() {
    return this.classData;
  }

  public void setClassData(final Map<String, ClassData2> classData) {
    this.classData = classData;
  }

  public Map<String, String> getFileMetric() {
    return this.fileMetric;
  }

  public void setFileMetric(final Map<String, String> fileMetric) {
    this.fileMetric = fileMetric;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public String getModifiedLines() {
    return this.modifiedLines;
  }

  public void setModifiedLines(final String modifiedLines) {
    this.modifiedLines = modifiedLines;
  }

  public String getAddedLines() {
    return this.addedLines;
  }

  public void setAddedLines(final String addedLines) {
    this.addedLines = addedLines;
  }

  public String getDeletedLines() {
    return this.deletedLines;
  }

  public void setDeletedLines(final String deletedLines) {
    this.deletedLines = deletedLines;
  }

  /**
   * A class for the class-respective meta-data and metrics.
   */
  public static class ClassData2 {

    private ClassType2 type;
    private List<String> modifier;
    private List<String> intrfc;
    private List<FieldData2> field;
    private List<String> innerClass;
    private List<MethodData2> constructor;
    private Map<String, MethodData2> methodData;
    private List<String> variable;
    private String superClass;
    private List<String> enumConstant;
    private List<String> annotation;
    private Map<String, String> classMetric;

    public ClassType2 getType() {
      return this.type;
    }

    public void setType(final ClassType2 type) {
      this.type = type;
    }

    public List<String> getModifier() {
      return this.modifier;
    }

    public void setModifier(final List<String> modifier) {
      this.modifier = modifier;
    }

    public List<String> getIntrfc() {
      return this.intrfc;
    }

    public void setIntrfc(final List<String> intrfc) {
      this.intrfc = intrfc;
    }

    public List<FieldData2> getField() {
      return this.field;
    }

    public void setField(final List<FieldData2> field) {
      this.field = field;
    }

    public List<String> getInnerClass() {
      return this.innerClass;
    }

    public void setInnerClass(final List<String> innerClass) {
      this.innerClass = innerClass;
    }

    public List<MethodData2> getConstructor() {
      return this.constructor;
    }

    public void setConstructor(final List<MethodData2> constructor) {
      this.constructor = constructor;
    }

    public Map<String, MethodData2> getMethodData() {
      return this.methodData;
    }

    public void setMethodData(final Map<String, MethodData2> methodData) {
      this.methodData = methodData;
    }

    public List<String> getVariable() {
      return this.variable;
    }

    public void setVariable(final List<String> variable) {
      this.variable = variable;
    }

    public String getSuperClass() {
      return this.superClass;
    }

    public void setSuperClass(final String superClass) {
      this.superClass = superClass;
    }

    public List<String> getEnumConstant() {
      return this.enumConstant;
    }

    public void setEnumConstant(final List<String> enumConstant) {
      this.enumConstant = enumConstant;
    }

    public List<String> getAnnotation() {
      return this.annotation;
    }

    public void setAnnotation(final List<String> annotation) {
      this.annotation = annotation;
    }

    public Map<String, String> getClassMetric() {
      return this.classMetric;
    }

    public void setClassMetric(final Map<String, String> classMetric) {
      this.classMetric = classMetric;
    }

    /**
     * An enum for the type of class.
     */
    public enum ClassType2 {
      INTERFACE,
      ABSTRACT_CLASS,
      CLASS,
      ENUM,
      ANONYMOUS_CLASS
    }

    /**
     * A class for the field meta-data.
     */
    public static class FieldData2 {

      private String name;
      private String type;
      private List<String> modifier;

      public String getName() {
        return this.name;
      }

      public void setName(final String name) {
        this.name = name;
      }

      public String getType() {
        return this.type;
      }

      public void setType(final String type) {
        this.type = type;
      }

      public List<String> getModifier() {
        return this.modifier;
      }

      public void setModifier(final List<String> modifier) {
        this.modifier = modifier;
      }
    }

    /**
     * A class for the method meta-data.
     */
    public static class MethodData2 {

      private String returnType;
      private List<String> modifier;
      private List<ParameterData2> parameter;
      private List<String> outgoingMethodCalls;
      private boolean constructor;
      private List<String> annotation;
      private Map<String, String> metric;

      public String getReturnType() {
        return this.returnType;
      }

      public void setReturnType(final String returnType) {
        this.returnType = returnType;
      }

      public List<String> getModifier() {
        return this.modifier;
      }

      public void setModifier(final List<String> modifier) {
        this.modifier = modifier;
      }

      public List<ParameterData2> getParameter() {
        return this.parameter;
      }

      public void setParameter(final List<ParameterData2> parameter) {
        this.parameter = parameter;
      }

      public List<String> getOutgoingMethodCalls() {
        return this.outgoingMethodCalls;
      }

      public void setOutgoingMethodCalls(final List<String> outgoingMethodCalls) {
        this.outgoingMethodCalls = outgoingMethodCalls;
      }

      public boolean isConstructor() {
        return this.constructor;
      }

      // public boolean getConstructor() { // NOPMD
      // return this.constructor;
      // }

      public void setConstructor(final boolean isConstructor) {
        this.constructor = isConstructor;
      }

      public List<String> getAnnotation() {
        return this.annotation;
      }

      public void setAnnotation(final List<String> annotation) {
        this.annotation = annotation;
      }

      public Map<String, String> getMetric() {
        return this.metric;
      }

      public void setMetric(final Map<String, String> metric) {
        this.metric = metric;
      }

      /**
       * A class for the parameter meta-data.
       */
      public static class ParameterData2 {

        private String name;
        private String type;
        private List<String> modifier;

        public String getName() {
          return this.name;
        }

        public void setName(final String name) {
          this.name = name;
        }

        public String getType() {
          return this.type;
        }

        public void setType(final String type) {
          this.type = type;
        }

        public List<String> getModifier() {
          return this.modifier;
        }

        public void setModifier(final List<String> modifier) {
          this.modifier = modifier;
        }
      }
    }
  }
}
