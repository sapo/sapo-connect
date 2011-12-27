---
title: SAPO Connect for Android
---

# SAPO Connect for Android

The SAPO Connect Android Library Project contains the implementation of the SAPO ID interface, so that a user can be authenticated and authorized to invoke an OAuth service registered in SAPO Bus.

The entire login process is executed within a WebView, modified from it's default values so that it has the following features: 

- The UserAgent has a special parameter so that the SAPO ID server can detect the difference between a native app (our app) from a web app;
- All redirects are made within the WebView, so that they don't open the native Web Browser, forcing the user to exit the application;
- When there's a redirect to the SAPO Home Page (usually in case of error or page not found), the WebView intercepts the page loading and doesn't show it to the user;
- Through the same process, the WebView intercepts several callbacks from the SAPO ID server, showing them or hiding then according to the situation, invoking during the process several native Android components to interact with the user;
- When a page is loading in the WebView, it is possible to show some sign of activity to the user through the use of native components (e.g. indeterminate progress in the title bar);
- When receiving an error from the server, it is showed a custom HTML error page;
- The WebView cache and the SavePassword feature are disabled;
- JavaScript is enabled;
- All cookies in the WebView will be deleted in the end of the authentication process, so that the user can login and logout with different credentials.

In the beginning of the OAuth process, a connection to a NTP server is established. If the connection was successful, the delta time between the NTP server time and the device time is determined. If this delta is bigger that 5 minutes, the authentication process will be interrupted, showing an AlertDialog to the user, informing that the device time has to be set correctly. This is due to the fact that every OAuth service call is made through the SAPO Bus, and they validate the timestamp in the OAuth signature against the timestamp of the server time. The SAPO Bus only allows a 5 minute window in the future and into the past in relation to the server time. E.g. if the device time is 16:05:02 and the server time is 16:00:00, we can not invoke the OAuth service due to a 2 second error in the allowed time window. If the device cannot get a connection to the NTP server, it was decided that the OAuth process was not to be interrupted. If this time check is not performed, and if the device clock is not within the allowed time frame, the OAuth request will result in an error, in which it will not be possible to determine it's cause, because SAPO Bus doesn't specify that particular error cause in the response.

# SAPO CONNECT INTEGRATION WITH MINIMUM CONFIGURATION

 1. Add to the Android application Java Build Path the three OAuth libraries that can be found in the "SAPO_Connect/lib". In Eclipse, go to "Project" > "Properties" > "Java Build Path" > Tab "Libraries" > "Add Jars"

  - oauth-20100527.jar
  - oauth-consumer-20100527.jar
  - oauth-httpclient4-20090913.jar

 2. Because the Assets of an Android Library Projects are not automatically included into the application, copy the HTML files in the folder "SAPO_Connect/assets" to the assets folder of the Android app:

  - empty.html
  - error.html

 These HTML pages can be customized according to each application.

 The "error.html" is showed to the user every time we have an error in the WebView. E.g., instead of showing an HTML page with a standard 404 error, it will be showed this error page in which the string %%ERROR%% will be replaced with the error cause.

 The "empty.html" page is only used to show inactivity in the WebView, or when we want to hide some page from the user. E.g., when the WebView is busy communicating with WebServices or when we intercept some callback of a non existing page. This page, like the error page, can also be customized.

 Both pages will be showed in the WebView with a transparent background, so that if the implementing app has some Theme with an image background, it will be visible.

 3. The next step is to register the Android application in the SAPO Connect backoffice. Go to [http://id.sapo.pt/connect](http://id.sapo.pt/connect) and login with your SAPO user account. Go through "Gerir as minhas aplicações" > "Criar nova aplicação".

 It will be necessary to define a name for the application and a Callback URL. This URL will be called by the SAPO ID server when the authentication process is completed, and it will be intercepted by the SAPO Connect WebView.
 We will also need to define the authorizations so that the Android app can access the desired resources. In the example below, the application "SAPO Connect Example Android" is given authorization to access the SAPO Bus resources of SAPO Fotos. 

   ![SAPO Connect Backoffice](images/connect.png)

 4. Create a services.xml in your app res folder and populate it with the values provided in the SAPO Connect backoffice, associating them with the following keys:

   ![XML values 1](images/xml1.png)

 5. Create an Activity that extends the abstract class SAPOConnect. E.g., MyAppSAPOConnect. Implement the required methods:

   ![Java Code 1](images/java1.png)

    And this is it. This is the minimum configuration for an Android app to use the SAPO Connect Android Library Project. With this, the application can now authenticate through SAPO ID and invoke WebServices in the SAPO Bus that require an OAuth signature.

# USAGE

The SAPO Connect for Android, allows:

- To determine if a user is logged-in;
- Login;
- Logout;
- Invoke WebServices with OAuth through the methods GET and POST (other methods may be added as they are required).

   1. Login:

     ![Java Code 2](images/java2.png)
 
   2. Logout:

     We can use two different modes for logging-out. An Intent based method, similar to the login:

     ![Java Code 3](images/java3.png)
	 
     And a more simple static method, that doesn't require to start an Activity and wait for a response. However, this one doesn't execute any additional logic defined in the LogOutInterface:

     ![Java Code 4](images/java4.png)
	 
   3. Invoke WebService with Oauth using a GET request:

     ![Java Code 5](images/java5.png)

     For using a POST request, you use the method 'invokeWebServicePost' passing the same parameters along with an additional string containing the post body. It is also possible to pass the parameters as name/value pairs, but that functionality has to be coded.

# SAPO CONNECT CUSTOMIZED INTEGRATION

It is possible to customize the SAPOConnect through the implementation of four interfaces:

- LogInInterface
- LogOutInterface
- WindowTitleBarControlInterface
- CustomNotificationsInterface

 1. LogInInterface

     If the application needs additional logic to be executed after the authentication process with the SAPO ID server, in which only after it's successful execution the login process can be said to be completed, it can define it in this interface implementation. E.g.:
     
	 ![Java Code 6](images/java6.png)
     
     Be aware that the SAPOConnect login process will only be completed when the following method is invoked:

	 ![Java Code 7](images/java7.png)
	 
     And when it pass the results to the Activity that is waiting for the login process to end:

	 ![Java Code 8](images/java8.png)

     So, if this interface is implemented, these two methods must be invoked at some time.

 2. LogOutInterface

     If the application needs additional logic to be executed after the logout process, it can define it in this interface implementation. E.g.:

	 ![Java Code 9](images/java9.png)
	 
 3. WindowTitleBarControlInterface

     If the application needs additional logic to show the user some kind of  progress when a web page is loading, it can specify it through the implementation of this interface. It is possible to configure a custom Window Title Bar through the 'getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_window_title_bar)' or to specify the 'FEATURE_INDETERMINATE_PROGRESS' in the Title Bar.

     E.g.: 
	 
	 ![Java Code 10](images/java10.png)
	 
 4. CustomNotificationsInterface

     The SAPOConnect Activity uses Toast notifications and AlertDialogs to interact with the user. If the application needs to customize the Toast and AlertDialog layouts, it can do it through the implementation of this interface. E.g.:
	 
	 ![Java Code 11](images/java11.png)
	 
     If this interface is not implemented, or if some of it's methods return null, the system default will be used.

     Mandatory rules to implement the custom layouts:

     i) Toast

     * Top container with android:id="@+id/toast_layout_root".
     * TextView with android:id="@+id/toast_text"

     ii) AlertDialog with two buttons:

     * Top container with android:id="@+id/customDialog_layoutRoot"
     * TextView with android:id="@+id/customDialog_text"
     * Positive Button with android:id="@+id/customDialog_okBtn"
     * Negative Button with android:id="@+id/customDialog_nokBtn"

     iii) AlertDialog with one button:

     * Top container with android:id="@+id/customDialog_layoutRoot"
     * TextView with android:id="@+id/customDialog_text"
     * Positive Button with android:id="@+id/customDialog_okBtn"

     The class that extends SAPOConnect can use these dialogs through the methods:
	 
	 ![Java Code 12](images/java12.png)
	 
     E.g.:

	 ![Java Code 13](images/java13.png)
	 
     Or:

	 ![Java Code 14](images/java14.png)
	 
     Or:
	 
	 ![Java Code 15](images/java15.png)
