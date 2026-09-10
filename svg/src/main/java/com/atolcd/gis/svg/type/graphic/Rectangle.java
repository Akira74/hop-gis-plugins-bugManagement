package com.atolcd.gis.svg.type.graphic;

import com.atolcd.gis.svg.type.AbstractGraphic;

public class Rectangle extends AbstractGraphic {

  private final double x;
  private final double y;
  private final double width;
  private final double height;
  private final double xRadius;
  private final double yRadius;

  public Rectangle(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.xRadius = Double.NaN;
    this.yRadius = Double.NaN;
  }

  public Rectangle(
      double x, double y, double width, double height, double xRadius, double yRadius) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.xRadius = xRadius;
    this.yRadius = yRadius;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public double getXRadius() {
    return xRadius;
  }

  public double getYRadius() {
    return yRadius;
  }
}
