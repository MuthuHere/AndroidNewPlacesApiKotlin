# AndroidNewPlacesApiKotlin
Android updated places API with Kotlin

https://ibb.co/9g5sNf6

 for places API init
 
        Places.initialize(this@MainActivity, resources.getString(R.string.api_key))
        placesClient = Places.createClient(this)
        val token = AutocompleteSessionToken.newInstance()
        
        
#### Token Generation
  We need to genearate token to access the places. 
  
      val token = AutocompleteSessionToken.newInstance()
        
        
### Calling on Every Text change
 
     val autoCompleteBuilder = FindAutocompletePredictionsRequest.builder()
                    .setCountry("IN")//Country your belongs to 
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)//we are generating token for place access
                    .setQuery(s.toString()) // your text which you typed
                    .build()
                    
                    
        
Used Libraries,
   
    1.implementation 'com.github.mancj:MaterialSearchBar:0.8.2'
    2.implementation 'com.karumi:dexter:5.0.0'
    3.implementation 'com.skyfishjy.ripplebackground:library:1.0.1
      
