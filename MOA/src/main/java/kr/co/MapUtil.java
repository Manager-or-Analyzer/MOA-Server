package kr.co;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.co.data.TF_IDF;
import kr.co.data.send.Snippet;

public class MapUtil<K,V> {
	public static <K, V extends Comparable<? super V>> Map<K, V> 
    		Map_sortByValue( Map<K, V> map ){
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>(){
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ){
				return (o2.getValue()).compareTo( o1.getValue() );
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list){
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, TF_IDF> sortRawdataAsSimilarity(Map rawdata, Map similarDoc){
		/*
		 * rawdata를 simildoc의 순서에 맞게 정렬해 주는 함수 
		 * 자료형에 종속적임.
		 */
		Map<String, TF_IDF> result = new LinkedHashMap<String, TF_IDF>();
		
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>( similarDoc.entrySet());
		for(Map.Entry<String, Double> entry : list){
			result.put( entry.getKey(), (TF_IDF) rawdata.get(entry.getKey()) );
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<TF_IDF> sortRawdataAsSimilarityInArray(Map rawdata, Map similarDoc){
		/*
		 * rawdata를 simildoc의 순서에 맞게 정렬해 주는 함수 
		 * 자료형에 종속적임.
		 */
		if(similarDoc == null){
			System.out.println("Error : similarDoc is null");
			return null;
		}
		List<TF_IDF> result = new ArrayList<TF_IDF> ();
		
		int index = 0;
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>( similarDoc.entrySet());
		for(Map.Entry<String, Double> entry : list){
			result.add( index++, (TF_IDF) rawdata.get(entry.getKey()) );
		}
		return result;
	}
}
