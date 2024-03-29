package com.avenord.podplay.db;

import java.lang.System;

@android.arch.persistence.room.Dao()
@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u00072\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0016\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\f2\u0006\u0010\r\u001a\u00020\u0007H\'J\u0012\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000f\u001a\u00020\u0010H\'J\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\f0\u0012H\'J\u000e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\fH\'\u00a8\u0006\u0014"}, d2 = {"Lcom/avenord/podplay/db/PodcastDao;", "", "deletePodcast", "", "podcast", "Lcom/avenord/podplay/model/Podcast;", "insertEpisode", "", "episode", "Lcom/avenord/podplay/model/Episode;", "insertPodcast", "loadEpisodes", "", "podcastId", "loadPodcast", "url", "", "loadPodcasts", "Landroid/arch/lifecycle/LiveData;", "loadPodcastsStatic", "app_debug"})
public abstract interface PodcastDao {
    
    @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.Query(value = "SELECT * FROM Podcast ORDER BY FeedTitle")
    public abstract android.arch.lifecycle.LiveData<java.util.List<com.avenord.podplay.model.Podcast>> loadPodcasts();
    
    @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.Query(value = "SELECT * FROM Podcast ORDER BY FeedTitle")
    public abstract java.util.List<com.avenord.podplay.model.Podcast> loadPodcastsStatic();
    
    @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.Query(value = "SELECT * FROM Episode WHERE podcastId = :podcastId ORDER BY releaseDate DESC")
    public abstract java.util.List<com.avenord.podplay.model.Episode> loadEpisodes(long podcastId);
    
    @org.jetbrains.annotations.Nullable()
    @android.arch.persistence.room.Query(value = "SELECT * FROM Podcast WHERE feedUrl = :url")
    public abstract com.avenord.podplay.model.Podcast loadPodcast(@org.jetbrains.annotations.NotNull()
    java.lang.String url);
    
    @android.arch.persistence.room.Insert(onConflict = android.arch.persistence.room.OnConflictStrategy.REPLACE)
    public abstract long insertPodcast(@org.jetbrains.annotations.NotNull()
    com.avenord.podplay.model.Podcast podcast);
    
    @android.arch.persistence.room.Insert(onConflict = android.arch.persistence.room.OnConflictStrategy.REPLACE)
    public abstract long insertEpisode(@org.jetbrains.annotations.NotNull()
    com.avenord.podplay.model.Episode episode);
    
    @android.arch.persistence.room.Delete()
    public abstract void deletePodcast(@org.jetbrains.annotations.NotNull()
    com.avenord.podplay.model.Podcast podcast);
}