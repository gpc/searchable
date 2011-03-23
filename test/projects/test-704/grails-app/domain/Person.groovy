class Person {

	String name
	List<Breed> _breeds

	
	static transients = [
		'_breeds',
		'breeds'
    ]
	
	
	
	static searchable = {
		
		only = ["name", "breeds", "dogs"]
		dogs component: true
		breeds component: true
	}
	
	
	static hasMany = [ dogs: Dog ]
	
	List<Breed> getBreeds() {
		
		if ( _breeds == null && dogs != null) {
			_breeds = dogs.collect{ dog ->
				dog.breed
			}
		}
		_breeds
	}
	
	void setBreeds(List<Breed> b) {
		
		_breeds = b
	}
		
}
