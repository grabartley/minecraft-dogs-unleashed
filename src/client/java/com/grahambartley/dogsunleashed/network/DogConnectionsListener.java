package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;

/**
 * Implemented by screens that consume {@link SyncDogConnectionsPayload} responses. The client
 * receiver only delivers to the currently open screen, so a response arriving after the requesting
 * screen closed is dropped.
 */
public interface DogConnectionsListener {

  void onDogConnections(SyncDogConnectionsPayload payload);
}
