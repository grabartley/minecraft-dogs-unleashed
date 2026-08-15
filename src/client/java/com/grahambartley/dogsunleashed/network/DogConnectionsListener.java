package com.grahambartley.dogsunleashed.network;

/**
 * Implemented by screens that consume {@link ModNetworking.SyncDogConnectionsPayload} responses.
 * The client receiver only delivers to the currently open screen, so a response arriving after the
 * requesting screen closed is dropped.
 */
public interface DogConnectionsListener {

  void onDogConnections(ModNetworking.SyncDogConnectionsPayload payload);
}
