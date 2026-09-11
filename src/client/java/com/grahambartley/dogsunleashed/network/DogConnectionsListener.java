package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;

public interface DogConnectionsListener {

  void onDogConnections(SyncDogConnectionsPayload payload);
}
