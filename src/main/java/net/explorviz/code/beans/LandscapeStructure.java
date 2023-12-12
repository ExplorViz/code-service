package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class LandscapeStructure {
  private String landscapeToken;
  private List<Node> nodes;

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public List<Node> getNodes() {
    return this.nodes;
  }

  public void setNodes(final List<Node> nodes) {
    this.nodes = nodes;
  }

  /**
   * ...
   */
  public static class Node {

    private String ipAdress;
    private String hostName;
    private List<Application> applications;

    public String getIpAdress() {
      return this.ipAdress;
    }

    public void setIpAdress(final String ipAdress) {
      this.ipAdress = ipAdress;
    }

    public String getHostName() {
      return this.hostName;
    }

    public void setHostName(final String hostName) {
      this.hostName = hostName;
    }

    public List<Application> getApplications() {
      return this.applications;
    }

    public void setApplications(final List<Application> applications) {
      this.applications = applications;
    }

    /**
     * ...
     */
    public static class Application {

      private String name;
      private String language;
      private String instanceId;
      private List<Package> packages;

      public String getName() {
        return this.name;
      }

      public void setName(final String name) {
        this.name = name;
      }

      public String getLanguage() {
        return this.language;
      }

      public void setLanguage(final String language) {
        this.language = language;
      }

      public String getInstanceId() {
        return this.instanceId;
      }

      public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
      }

      public List<Package> getPackages() {
        return this.packages;
      }

      public void setPackages(final List<Package> packages) {
        this.packages = packages;
      }


      /**
       * ...
       */
      public static class Package {

  
        private String name;
        private List<Package> subPackages;
        private List<Class> classes;

        public String getName() {
          return this.name;
        }

        public void setName(final String name) {
          this.name = name;
        }

        public List<Package> getSubPackages() {
          return this.subPackages;
        }

        public void setSubPackages(final List<Package> subPackages) {
          this.subPackages = subPackages;
        }

        public List<Class> getClasses() {
          return this.classes;
        }

        public void setClasses(final List<Class> classes) {
          this.classes = classes;
        }

        /**
          * ...
          */
        public static class Class {

          private String name;
          private List<Method> methods;
          private String superClass; /*  full qualified class name. 
                                       TODO: refactor so it is from type Class? */

          public String getName() {
            return this.name;
          }

          public void setName(final String name) {
            this.name = name;
          }

          public List<Method> getMethods() {
            return this.methods;
          }

          public void setMethods(final List<Method> methods) {
            this.methods = methods;
          }

          public String getSuperClass() {
            return this.superClass;
          }

          public void setSuperClass(final String superClass) {
            this.superClass = superClass;
          }

          /**
           * ...
           */
          public static class Method {

            private String name;
            private String hashCode;

            public String getName() {
              return this.name;
            }

            public void setName(final String name) {
              this.name = name;
            }

            public String getHashCode() {
              return this.hashCode;
            }

            public void setHashCode(final String hashCode) {
              this.hashCode = hashCode;
            }
          }
        }
      }
    }
  }
}