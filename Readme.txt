A2 - Covid volunteering site
Nguyen Hoang Khang s3802040

Super user account for testing:
gmail: super@gmail.com
password: 123456

1. Functionalities:

- The ability for the leader to create accounts, set up locations, download lists of people who join their campaigns, input data after the campaigns how much people were tested or modifying the details of the his site

- The volunteer to view registered locations on a map, click on locations, register themselves and friends (if the friend has the account in the system) to a site.

- The super user is able see the outcomes of the different locations the number of volunteers and amount of people tested. He can also modify the details of any sites he clicked into. The download is also 

- A map view that shows nearby all available volunteering sites

- The number of sites to be shown will depend on the zoom level and camera view. 

- Users can search sites based on title and leader's name without logging in needed

- Users can join a site by creating an account and sign in

- Users can create new site and be leader of that site. As the leader of the site, user can see the list of people who sign up for the site.

- 1 leader can host many sites

- There will be notifications to the leader when there are changes in a site

- Super user of the app can see all sites.

- Can find routes from current user location to the site

- search place on the world


2. Technology used:


- Notification: is used to notify the changes in the site

- Dialog: is used for popping up the information of the action in the map

- Map: is used to show the map to the user

- Direction API: is used in routing which will find routes from the current user location to the site.

- Place API: is used in searching places in map.

- Firebase Realtime Database: is used in storing objects in the database. Automatically fetching and writing the data whenever the change occurs.

- Firebase Authentication: is used in validating the sign in and sign up action.

- CustomListAdapter: is used in customizing the list of volunteers in a site.

- Cluster: is used in clustering the site together without making the map hard to read.

