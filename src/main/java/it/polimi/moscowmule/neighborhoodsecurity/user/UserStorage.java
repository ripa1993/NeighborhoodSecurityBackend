package it.polimi.moscowmule.neighborhoodsecurity.user;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.DatabaseUsers;

public enum UserStorage {
	instance;
	
	public int addWithPassword(User u, String password){
		int id = DatabaseUsers.createUser(u);
		if(id > 0){
			DatabaseUsers.createLogin(id, password);
		}
		return id;
	}
	
	public int addWithoutPassword(User u){
		return DatabaseUsers.createUser(u);
	}
	
	public User getById(int id){
		return DatabaseUsers.getById(id);
	}
	
	public boolean remove(int id){
		return DatabaseUsers.removeUser(id);
	}
}
