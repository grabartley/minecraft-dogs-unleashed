package com.grahambartley.dogsunleashed.screen;

interface ConfigSection<T> {

  void addTo(DogsUnleashedConfigList list, ConfigRows rows);

  T settings();
}
