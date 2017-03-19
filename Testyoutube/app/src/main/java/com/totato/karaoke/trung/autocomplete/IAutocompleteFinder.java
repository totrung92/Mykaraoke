package com.totato.karaoke.trung.autocomplete;

import java.util.List;

public interface IAutocompleteFinder {

	public void success(String search, List<String> items);
	
	public void error(String response);
	
}
