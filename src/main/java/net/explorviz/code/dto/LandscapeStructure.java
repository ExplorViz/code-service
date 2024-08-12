package net.explorviz.code.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for the static landscape structure used for a commit.
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

    private String ipAddress;
    private String hostName;
    private List<Application> applications;

    public String getIpAddress() {
      return this.ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
      this.ipAddress = ipAddress;
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
       * Helper class.
       */
      public static class Package {


        private String name;
        private List<Package> subPackages = new ArrayList<>();
        private List<Class> classes = new ArrayList<>();

        public Package() {

        }

        public Package(final String name) {
          this.name = name;
        }

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
         * Override for toString.
         *
         * @return String reprenstation
         */
        public String toString() {
          if (this.getSubPackages().isEmpty()) {
            return this.getName();
          }

          StringBuilder retString = new StringBuilder();
          for (final Package subPackage : this.getSubPackages()) {
            retString.append(this.getName()).append("->").append(subPackage.toString())
                .append("\n");
          }
          return retString.toString();
        }

        /**
         * ...
         */
        public static class Class {

          private String name;
          private List<Method> methods = new ArrayList<>();
          private String superClass; /*  full qualified class name. 
                                       TODO: refactor so it is from type Class? */

          public Class() {
          }

          public Class(final String name) {
            this.name = name;
          }

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
            private String methodHash;

            public String getName() {
              return this.name;
            }

            public void setName(final String name) {
              this.name = name;
            }

            public String getMethodHash() {
              return this.methodHash;
            }

            public void setMethodHash(final String hashCode) {
              this.methodHash = hashCode;
            }
          }
        }
      }
    }
  }
}
