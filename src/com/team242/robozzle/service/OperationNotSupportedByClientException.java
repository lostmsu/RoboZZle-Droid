package com.team242.robozzle.service;

/**
 * Created by lost on 2/28/2016.
 */
public class OperationNotSupportedByClientException extends Exception {
	public OperationNotSupportedByClientException(Throwable cause){
		super(cause);
	}
}
