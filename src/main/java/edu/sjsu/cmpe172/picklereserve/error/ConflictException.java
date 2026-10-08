package edu.sjsu.cmpe172.picklereserve.error;
public class ConflictException extends RuntimeException { public ConflictException(String m){super(m);} public ConflictException(String m, Throwable t){super(m,t);} }
