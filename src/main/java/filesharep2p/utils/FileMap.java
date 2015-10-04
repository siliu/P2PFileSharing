package filesharep2p.utils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author siliu
 * 
 *
 */
public class FileMap {
	
	/*
	 * The executeRegistry() function is to implement the peer file registration.
	 * Map is used to maintain the peer file registration table
	 * There are two scenarios need to be considered:
	 * 1. If the peer is already in already registered, its file list should be updated by registering new files on that peer
	 * 2. If the peer is not registered yet, new map entry should be added to the registration map
	 */
	public static void executeRegistry(Map<String, Object> newPeerFileMap, Map<String, Object> oldPeerFileMap){
		
		Set<String> newFileSet = newPeerFileMap.keySet();
		Set<String> oldFileSet = oldPeerFileMap.keySet();
		
		for(String file : newFileSet){
			if(oldFileSet.contains(file)){
				
				Set<Long> peerList = new HashSet<Long>();
				
				for(Long peerId : (Set<Long>) oldPeerFileMap.get(file)){
					peerList.add(peerId);
				}
				
				for(Long peerId : (Set<Long>) newPeerFileMap.get(file)){
					peerList.add(peerId);
				}
				
				oldPeerFileMap.put(file, peerList);
				
			}else{
				
				Set<Long> peerList = new HashSet<Long>();
				
				for(Long peerId : (Set<Long>) newPeerFileMap.get(file)){
					peerList.add(peerId);
				}
				
				oldPeerFileMap.put(file, peerList);
			}	
		}
	} //end executeRegistry()
	
	/*
	 * The executeSearch function is to implement the procedure to search the file and return all the mactching peers.
	 */
	public static void  executeSearch(Map<String, Object> resultPeerFileMap, Map<String, Object> oldPeerFileMap){
		
		Set<String> resultFileSet = resultPeerFileMap.keySet();
		Set<String> oldFileSet = oldPeerFileMap.keySet();
		
		for(String file : oldFileSet){
			if(resultFileSet.contains(file)){
				Set<Long> peerList = new HashSet<Long>();
				for(Long peerId:  (Set<Long>) oldPeerFileMap.get(file)){
					peerList.add(peerId);
				}
				resultPeerFileMap.put(file, peerList);
			}
		}
	}// end executeSearch()
	
	private static Set<Long> listToSet(List list){
		Set<Long> set = new HashSet<Long>();
		for(int i=0; i<list.size(); i++)
			set.add(Math.round((Double)list.get(i)));
		return set;
	}
	
	/*
	 * The executionDelete function is to implement the procedure to delete peer registration record from the registration table
	 */
	public static void executeDelete(Map<String, Object> deletePeerFileMap, Map<String, Object> oldPeerFileMap){
		
		Set<String> deleteFileSet = deletePeerFileMap.keySet();
		Set<String> oldFileSet = oldPeerFileMap.keySet();
		
		for(String file : deleteFileSet){
			if(oldFileSet.contains(file)){
				
				Set<Long> peerList = new HashSet<Long>();
				
				for(Long peerId : (Set<Long>) oldPeerFileMap.get(file)){
					peerList.add(peerId);
				}
				
				Set<Long> deletePeerSet = listToSet((List) deletePeerFileMap.get(file));
				for(Long peerId : deletePeerSet){
					peerList.remove(peerId);
				}
				
				oldPeerFileMap.put(file, peerList);
				
			}
		}
		
		for(String file: oldPeerFileMap.keySet()){
			if(oldPeerFileMap.get(file)== null){
				oldPeerFileMap.remove(file);
			}
		}
		
	} //end executeDelete()
	
	public static void printMap(Map<String, Object> map) {

        Set<String> mapKeySet = map.keySet();
        for (String key : mapKeySet) {
            System.out.print(key + ": ");
            for (Long value : (Set<Long>) map.get(key)) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
        System.out.println();
    }//end printMap()
}






































