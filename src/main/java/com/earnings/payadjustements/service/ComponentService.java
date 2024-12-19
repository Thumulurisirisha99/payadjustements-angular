package com.earnings.payadjustements.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.earnings.payadjustements.entity.PayComponentRepository;

@Service   
public class ComponentService {
   
	@Autowired
	private PayComponentRepository componentRepository;

	public Map<String, Integer> getPayComponentMapByComponentNames(List<String> componentNames, int componenttypeid) {
		List<Object[]> results = componentRepository.findPayComponentByComponentNames(componentNames, componenttypeid);
		return results.stream().collect(Collectors.toMap(result -> (String) result[0], result -> (Integer) result[1]));
	}

}