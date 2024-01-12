package org.octopus.analysers;

public interface Analyser<T, V> {
    public V analyze(T code) throws Exception;
}
