package net.explorviz.code.watch;

import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class WatchFolderRoute extends RouteBuilder {

  @ConfigProperty(name = "explorviz.watchservice.folder")
  String folderPath;

  @ConfigProperty(name = "explorviz.watchservice.events")
  String events;

  @Override
  public void configure() throws Exception {
    this.fromF("file-watch://%s?events=%s&antInclude=**/*.java", this.folderPath, this.events)
        .process(new WatchFolderProcessor());
  }

}
