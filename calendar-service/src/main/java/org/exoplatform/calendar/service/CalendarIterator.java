package org.exoplatform.calendar.service;

import java.util.Iterator;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;

public class CalendarIterator implements Iterator {

	private NodeIterator uItor;
	private NodeIterator pItor;
	private PropertyIterator shItor;
	
    private long current = 0;
    private long size = 0;
    private boolean isNode = true;
    
     

    public boolean hasNext() {
    	if(uItor.hasNext()) return uItor.hasNext();
    	else if(pItor.hasNext()) return pItor.hasNext();
    	else if(shItor.hasNext()) return shItor.hasNext();
    	return false;
    }

    public Object next() {
    	Object it = null;
    	if(uItor.hasNext()) {
    		current++;
    		return uItor.nextNode();
    	}
    	else if(pItor.hasNext()) {
    		current++;
    		return pItor.nextNode();
    	}
    	else if(shItor.hasNext()) {
    		current++;
    		isNode = false;
    		return shItor.nextProperty();
    	}
    	 return it;
    }

    public void addShareIterator(PropertyIterator it){
     shItor = it;
     size+= it.getSize();
    }
    public void addPeronalIterator(NodeIterator it){
   	 uItor = it;
   	 size+= it.getSize();
   }
    public void addPublicIterator(NodeIterator it){
     pItor = it;
     size+= it.getSize();
   }
    public long getSize(){
    	return size;
    }
    public void remove() { /* not implemented */ }

	public long getCurrent() {
		return current;
	}

	public boolean isNode() {
		return isNode;
	}
}
