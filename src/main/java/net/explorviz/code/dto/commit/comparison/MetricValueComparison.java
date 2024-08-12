package net.explorviz.code.dto.commit.comparison;

public class MetricValueComparison {

  private String oldValue;
  private String newValue;

  public String getOldValue() {
    return this.oldValue;
  }

  public void setOldValue(final String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return this.newValue;
  }

  public void setNewValue(final String newValue) {
    this.newValue = newValue;
  }

}
