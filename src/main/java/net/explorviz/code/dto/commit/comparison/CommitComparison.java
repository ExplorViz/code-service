package net.explorviz.code.dto.commit.comparison;

import java.util.List;

public class CommitComparison {

  private List<String> added;
  private List<String> modified;
  private List<String> deleted;
  private List<String> addedPackages;
  private List<String> deletedPackages;
  private List<Metric> metrics;

  public CommitComparison(final List<String> added, final List<String> modified,
      final List<String> deleted, final List<String> addedPackages,
      final List<String> deletedPackages, final List<Metric> metrics) {
    this.added = added;
    this.modified = modified;
    this.deleted = deleted;
    this.addedPackages = addedPackages;
    this.deletedPackages = deletedPackages;
    this.metrics = metrics;
  }

  public List<String> getAdded() {
    return this.added;
  }

  public void setAdded(final List<String> added) {
    this.added = added;
  }

  public List<String> getModified() {
    return this.modified;
  }

  public void setModified(final List<String> modified) {
    this.modified = modified;
  }

  public List<String> getDeleted() {
    return this.deleted;
  }

  public void setDeleted(final List<String> deleted) {
    this.deleted = deleted;
  }

  public List<String> getAddedPackages() {
    return this.addedPackages;
  }

  public void setAddedPackages(final List<String> addedPackages) {
    this.addedPackages = addedPackages;
  }

  public List<String> getDeletedPackages() {
    return this.deletedPackages;
  }

  public void setDeletedPackages(final List<String> deletedPackages) {
    this.deletedPackages = deletedPackages;
  }

  public List<Metric> getMetrics() {
    return this.metrics;
  }

  public void setMetrics(final List<Metric> metrics) {
    this.metrics = metrics;
  }

}
