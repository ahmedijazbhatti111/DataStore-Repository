# DataStore-Repository
This class helps to get or put all types of data through jetpack data store using kotlin language

** Dependencies**

implementation "androidx.datastore:datastore-preferences:1.0.0"
 
implementation 'com.google.code.gson:gson:2.8.7'
 
** Initilization**

val datastoreRepo = DatastoreRepo.getInstance(context)
