package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class LandscapeStructure {
  public String landscapeToken;
  public List<Node> nodes;

  /**
   * ...
   */
  public static class Node {
    public String ipAdress;
    public String hostName;
    public List<Application> applications;

    /**
     * ...
     */
    public static class Application {
      public String name;
      public String language;
      public String instanceId;
      //public Node parent;
      public List<Package> packages;

      /**
       * ...
       */
      public static class Package {
        public String name;
        public List<Package> subPackages;
        public List<Class> classes;

        /**
          * ...
          */
        public static class Class {
          public String name;
          public List<Method> methods;
          public String superClass; /*  full qualified class name. 
                                       TODO: refactor so it is from type Class */

          /**
           * ...
           */
          public static class Method {
            public String name;
            public String hashCode;
          }
        }
      }
    }
  }
}