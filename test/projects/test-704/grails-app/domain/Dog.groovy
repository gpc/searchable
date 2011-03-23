class Dog {

	String name
	
	static searchable = {
		
		only = ["name", "breed"]
	}
	
	static belongsTo = [ owner: Person, breed: Breed ]
}
