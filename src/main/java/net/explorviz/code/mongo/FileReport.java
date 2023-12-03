package net.explorviz.code.mongo;

import java.util.List;
import java.util.Map;

/**
 *  ...
 */
public class FileReport {

  public String commitId;
  public String fileName;
  public String packageName;
  public List<String> importName;
  public Map<String, ClassData> classData;
  public Map<String, String> fileMetric;
  public String author;
  public int modifiedLines;
  public int addedLines;
  public int deletedLines;

  /**
   * ...
   */
  public static class ClassData {
    public ClassType type;
    public List<String> modifier;
    public List<String> intrfc;
    public List<FieldData> field;
    public List<String> innerClass;
    public List<MethodData> constructor;
    public Map<String, MethodData>  methodData;
    public List<String> variable;
    public String superClass;
    public List<String> enumConstant;
    public List<String> annotation;
    public Map<String, String> classMetric;

    /**
     * ...
     */
    public enum ClassType {
        INTERFACE,
        ABSTRACT_CLASS,
        CLASS,
        ENUM,
        ANONYMOUS_CLASS;
    }

    /**
     * ...
     */
    public static class FieldData {
      public String name;
      public String type;
      public String modifier;
    }

    /**
     * ...
     */
    public static class MethodData {
      public String returnType;
      public List<String> modifier;
      public List<ParameterData> parameter;
      public List<String> outgoingMethodCalls;
      public boolean isConstructor;
      public List<String> annotation;
      public Map<String, String> metric;

      /**
       * ...
       */
      public static class ParameterData {
        public String name;
        public String type;
        public List<String> modifier;
      }
    }
  }
}


