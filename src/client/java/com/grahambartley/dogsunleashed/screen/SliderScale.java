package com.grahambartley.dogsunleashed.screen;

record SliderScale(double min, double max) {

  double clamp(final double value) {
    return Math.max(this.min, Math.min(this.max, value));
  }

  double fractionOf(final double value) {
    return (clamp(value) - this.min) / (this.max - this.min);
  }

  double valueAt(final double fraction) {
    return this.min + (this.max - this.min) * fraction;
  }
}
