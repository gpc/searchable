class BootStrap {

     def init = { servletContext ->
	
		try {
			Dog d1 = new Dog(name:"Charlie")
			d1.breed = new Breed(name:"Boxer").save()
			d1.save()
			Dog d2 = new Dog(name: "Hector")
			d2.breed = new Breed(name:"German Shepherd").save() 
			d2.save()
			Dog d3 = new Dog(name:"Henry")
			d3.breed = new Breed(name:"Bloodhound").save()
			d3.save()
		
			Person p1 = new Person(name: "Jimmy John")
			p1.addToDogs(d1)
			p1.addToDogs(d2)
			p1.save()

			Person p2 = new Person(name: "Sammy Stiller")
			p2.addToDogs(d1)
			p2.addToDogs(d3)
			p2.save()
		}
		catch(Exception e){
			System.out.println(e.message)
		}			
	
	
     }
     def destroy = {
     }
} 