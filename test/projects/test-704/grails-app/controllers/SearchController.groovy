class SearchController {

    def index = {
	
		def p = Person.findByName("Jimmy John")
		render "Found person: " + p.name + " " + p.id	
	
	}

	def testFind = {
		
		def p = Person.findByName("Jimmy John")
		render "Found person: " + p.name + " " + p.id
	}
}
