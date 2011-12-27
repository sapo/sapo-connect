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

 ![SAPO Connect Backoffice](connect.png)

4. Create a services.xml in your app res folder and populate it with the values provided in the SAPO Connect backoffice, associating them with the following keys:

![service xml values](images/xml1.png)

5. Create an Activity that extends the abstract class SAPOConnect. E.g., MyAppSAPOConnect. Implement the required methods:

    <div class="code">
    <pre><span class="kd">public</span> <span
 class="kd">class</span> <span class="nc">MyAppSAPOConnect</span> <span
 class="kd">extends</span> SAPOConnect <span
 class="o">{</span><br>    <span class="nd">@Override</span> <span
 class="kd">public</span> LogInInterface getAditionalLogInOperations<span
 class="o">()</span> <span class="o">{</span> <span
 class="k">return</span> <span class="kc">null</span><span
 class="o">;</span> <span class="o">}</span><br>    <span
 class="nd">@Override</span> <span class="kd">public</span> LogOutInterface getAditionalLogOutOperations<span
 class="o">()</span> <span class="o">{</span> <span
 class="k">return</span> <span class="kc">null</span><span
 class="o">;</span> <span class="o">}</span><br>    <span
 class="nd">@Override</span> <span class="kd">public</span> WindowTitleBarControlInterface getWindowTitleBarControl<span
 class="o">()</span> <span class="o">{</span> <span
 class="k">return</span> <span class="kc">null</span><span
 class="o">;</span> <span class="o">}</span><br>    <span
 class="nd">@Override</span> <span class="kd">public</span> CustomNotificationsInterface getCustomNotificationsLayouts<span
 class="o">()</span> <span class="o">{</span> <span
 class="k">return</span> <span class="kc">null</span><span
 class="o">;</span> <span class="o">}</span><br><span
 class="o">}</span>
    </pre>
    </div>

    And this is it. This is the minimum configuration for an Android app to use the SAPO Connect Android Library Project. With this, the application can now authenticate through SAPO ID and invoke WebServices in the SAPO Bus that require an OAuth signature.

# USAGE

The SAPO Connect for Android, allows:

- To determine if a user is logged-in;
- Login;
- Logout;
- Invoke WebServices with OAuth through the methods GET and POST (other methods may be added as they are required).

 1. Login:

     <div class="code">
    <pre><span class="k">if</span> <span
 class="o">(!</span>MyAppSAPOConnect<span class="o">.</span><span
 class="na">isUserLoggedIn</span><span class="o">(</span>getApplicationContext<span
 class="o">()))</span> <span class="o">{</span><br>    <span
 class="c1">// The user isn't LoggedIn yet. Go to SAPO Connect.<br></span>    Intent goToLogin <span
 class="o">=</span> <span class="k">new</span> Intent<span
 class="o">(</span><span class="k">this</span><span
 class="o">,</span> MyAppSAPOConnect<span class="o">.</span><span
 class="na">class</span><span class="o">);</span><br>    goToLogin<span
 class="o">.</span><span class="na">putExtra</span><span
 class="o">(</span>SAPOConnect<span class="o">.</span><span
 class="na">SAPO_CONNECT_OPERATION</span><span
 class="o">,</span> SAPOConnect<span class="o">.</span><span
 class="na">SAPO_CONNECT_LOGIN</span><span class="o">);</span><br>    startActivityForResult<span
 class="o">(</span>goToLogin<span class="o">,</span> REQUEST_CODE_LOGIN<span
 class="o">);</span><br><span class="o">}</span> <span
 class="k">else</span> <span class="o">{</span><br>    <span
 class="c1">// The user is logged in. Do your stuff.<br></span><span
 class="o">}</span><br><br><span
 class="nd">@Override</span><br><span
 class="kd">protected</span> <span class="kt">void</span> <span
 class="nf">onActivityResult</span><span class="o">(</span><span
 class="kt">int</span> requestCode<span class="o">,</span> <span
 class="kt">int</span> resultCode<span class="o">,</span> Intent data<span
 class="o">)</span> <span class="o">{</span><br>    <span
 class="k">if</span> <span class="o">(</span>data <span
 class="o">==</span> <span class="kc">null</span><span
 class="o">)</span> <span class="o">{</span><br>       <span
 class="c1">// User pressed BACK key. Do some stuff if you have to.<br></span> <br>    <span
 class="o">}</span> <span class="k">else</span> <span
 class="o">{</span><br>       <span class="k">if</span> <span
 class="o">(</span>resultCode <span class="o">==</span> RESULT_CANCELED<span
 class="o">)</span> <span class="o">{</span><br>          <span
 class="k">switch</span> <span class="o">(</span>requestCode<span
 class="o">)</span> <span class="o">{</span><br>             <span
 class="k">case</span> <span class="nl">REQUEST_CODE_LOGIN:</span><br>             <span
 class="c1">// The Login was unsuccessful.<br></span>             <span
 class="k">break</span><span class="o">;</span><br>          <span
 class="o">}</span> <br>       <span class="o">}</span> <span
 class="k">else</span> <span class="o">{</span><br>          <span
 class="k">switch</span> <span class="o">(</span>requestCode<span
 class="o">)</span> <span class="o">{</span><br>             <span
 class="k">case</span> <span class="nl">REQUEST_CODE_LOGIN:</span><br>             <span
 class="c1">// Login was completed!<br></span>             <span
 class="k">break</span><span class="o">;</span><br>          <span
 class="o">}</span><br>       <span class="o">}</span><br>    <span
 class="o">}</span><br><span class="o">}</span>
    </pre>
    </div>

   2. Logout:

     We can use two different modes for logging-out. An Intent based method, similar to the login:

     <div class="code">
    <pre>Intent logOut <span class="o">=</span> <span
 class="k">new</span> Intent<span class="o">(</span><span
 class="k">this</span><span class="o">,</span> MyAppSAPOConnect<span
 class="o">.</span><span class="na">class</span><span
 class="o">);</span><br>logOut<span class="o">.</span><span
 class="na">putExtra</span><span class="o">(</span>SAPOConnect<span
 class="o">.</span><span class="na">SAPO_CONNECT_OPERATION</span><span
 class="o">,</span> SAPOConnect<span class="o">.</span><span
 class="na">SAPO_CONNECT_LOGOUT</span><span class="o">);</span><br>startActivity<span
 class="o">(</span>logOut<span class="o">);</span>
    </pre>
    </div>

     And a more simple static method, that doesn't require to start an Activity and wait for a response. However, this one doesn't execute any additional logic defined in the LogOutInterface:

     <div class="code">
    <pre>MyAppSAPOConnect<span class="o">.</span><span
 class="na"></span>simpleLogOut(getApplicationContext<span
 class="o">()</span>);<span class="o"></span>
    </pre>
    </div>

   3. Invoke WebService with Oauth using a GET request:

     <div class="code">
    <pre>String url <span class="o">=</span> <span
 class="s">"http://some.url/some.service?some=params"</span><span
 class="o">;</span> <br><span class="k">try</span> <span
 class="o">{</span><br>    String responseString <span
 class="o">=</span> MyAppSAPOConnect<span class="o">.</span><span
 class="na">invokeWebServiceGet</span><span class="o">(</span>getApplicationContext<span
 class="o">(),</span> url<span class="o">);</span><br><span
 class="o">}</span> <span class="k">catch</span> <span
 class="o">(</span>IOException e<span class="o">)</span> <span
 class="o">{</span><br><span class="o">}</span> <span
 class="k">catch</span> <span class="o">(</span>OAuthException e<span
 class="o">)</span> <span class="o">{</span><br><span
 class="o">}</span> <span class="k">catch</span> <span
 class="o">(</span>URISyntaxException e<span
 class="o">)</span> <span class="o">{</span><br><span
 class="o">}</span> <span class="k">catch</span> <span
 class="o">(</span>SapoKitException e<span class="o">)</span> <span
 class="o">{</span><br>    <span class="c1">// Impossible to retrieve OAuth credentials stored in SharedPreferences.<br></span><span
 class="o">}</span>
    </pre>
    </div>

     For using a POST request, you use the method 'invokeWebServicePost' passing the same parameters along with an additional string containing the post body. It is also possible to pass the parameters as name/value pairs, but that functionality has to be coded.

# SAPO CONNECT CUSTOMIZED INTEGRATION

It is possible to customize the SAPOConnect through the implementation of four interfaces:

- LogInInterface
- LogOutInterface
- WindowTitleBarControlInterface
- CustomNotificationsInterface

 1. LogInInterface

     If the application needs additional logic to be executed after the authentication process with the SAPO ID server, in which only after it's successful execution the login process can be said to be completed, it can define it in this interface implementation. E.g.:

     <div class="code">
  <pre><span class="kd">private</span> LogInInterface logInInterface <span
 class="o">=</span> <span class="k">new</span> LogInInterface<span
 class="o">()</span> <span class="o">{</span><br>    <span
 class="nd">@Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">logIn</span><span class="o">(</span>Context context<span
 class="o">)</span> <span class="o">{</span><br>        <span
 class="k">if</span> <span class="o">(</span>someOperation<span
 class="o">(</span>context<span class="o">))</span> <span
 class="o">{</span><br>           setUserRegistered<span
 class="o">(</span>context<span class="o">,</span> <span
 class="kc">true</span><span class="o">);</span><br>           goBackWithResults<span
 class="o">(</span><span class="kc">true</span><span
 class="o">);</span><br>       <span class="o"> }</span> <span
 class="k">else</span> <span class="o">{</span><br>           goBackWithResults<span
 class="o">(</span><span class="kc">false</span><span
 class="o">);</span><br>       <span class="o"> }</span><br>    <span
 class="o">}</span><br><span class="o">};</span><br><br><span
 class="nd">@Override</span><br><span
 class="kd">public</span> LogInInterface <span
 class="nf">getAditionalLogInOperations</span><span
 class="o">()</span> <span class="o">{</span><br>    <span
 class="k">return</span> logInInterface<span
 class="o">;</span><br><span class="o">}</span>
  </pre>
  </div>

     Be aware that the SAPOConnect login process will only be completed when the following method is invoked:

     <div class="code">
  <pre>setUserRegistered<span class="o">(</span>context<span
 class="o">,</span> <span class="kc">true</span><span
 class="o">);</span>
  </pre>
  </div>

     And when it pass the results to the Activity that is waiting for the login process to end:

     <div class="code">
  <pre>goBackWithResults<span class="o">(</span><span
 class="kc">true</span><span class="o">|</span><span
 class="kc">false</span><span class="o">);</span>
  </pre>
  </div>

     So, if this interface is implemented, these two methods must be invoked at some time.

 2. LogOutInterface

     If the application needs additional logic to be executed after the logout process, it can define it in this interface implementation. E.g.:

     <div class="code">
  <pre><span class="kd">private</span> LogOutInterface logOutInterface <span
 class="o">=</span> <span class="k">new</span> LogOutInterface<span
 class="o">()</span> <span class="o">{</span><br>    <span
 class="nd">@Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">logOut</span><span class="o">(</span>Context context<span
 class="o">)</span> <span class="o">{</span><br>        removePublicProfile<span
 class="o">(</span>context<span class="o">);</span><br>    <span
 class="o">}</span><br><span class="o">};</span><br> <br><span
 class="nd">@Override</span><br><span
 class="kd">public</span> LogOutInterface <span
 class="nf">aditionalLogOutOperations</span><span
 class="o">()</span> <span class="o">{</span><br>    <span
 class="k">return</span> logOutInterface<span
 class="o">;</span><br><span class="o">}</span>
  </pre>
  </div>

 3. WindowTitleBarControlInterface

     If the application needs additional logic to show the user some kind of  progress when a web page is loading, it can specify it through the implementation of this interface. It is possible to configure a custom Window Title Bar through the 'getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_window_title_bar)' or to specify the 'FEATURE_INDETERMINATE_PROGRESS' in the Title Bar.

     E.g.: 

     <div class="code">
  <pre><span class="kd">private</span> WindowTitleBarControlInterface windowTitleBarControlInterface <span
 class="o">=</span> <span class="k">new</span> WindowTitleBarControlInterface<span
 class="o">()</span> <span class="o">{</span><br>    @Override<br>    public int[] getWindowFeatures() {<br>        return new int[]{ Window.FEATURE_INDETERMINATE_PROGRESS };<br>    }<br><br>    <span
 class="nd">@Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">stopRefreshAnimation</span><span
 class="o">()</span> <span class="o">{</span><br>        setProgressBarIndeterminateVisibility<span
 class="o">(</span><span class="kc">false</span><span
 class="o">);</span><br>    <span class="o">}</span><br><br>    <span
 class="nd">@Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">startRefreshAnimation</span><span
 class="o">()</span> <span class="o">{</span><br>        setProgressBarIndeterminateVisibility<span
 class="o">(</span><span class="kc">true</span><span
 class="o">);</span><br>    <span class="o">}</span><br><br>    <span
 class="nd">@Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">setUpWindowTitleBar</span><span class="o">()</span> <span
 class="o">{</span><span class="o">};</span><br><span
 class="o">};</span><br><br><span
 class="nd">@Override</span><br><span
 class="kd">public</span> WindowTitleBarControlInterface <span
 class="nf">getWindowTitleBarControl</span><span
 class="o">()</span> <span class="o">{</span><br>    <span
 class="k">return</span> windowTitleBarControlInterface<span
 class="o">;</span><br><span class="o">}</span>
  </pre>
  </div>

 4. CustomNotificationsInterface

     The SAPOConnect Activity uses Toast notifications and AlertDialogs to interact with the user. If the application needs to customize the Toast and AlertDialog layouts, it can do it through the implementation of this interface. E.g.:

     <div class="code">
  <pre><span class="kd">private</span> CustomNotificationsInterface customNotificationsInterface <span
 class="o">=</span> <span class="k">new</span> CustomNotificationsInterface<span
 class="o">()</span> <span class="o">{</span><br> <span
 class="nd">   @Override</span><br><span
 class="nd">   </span> <span class="kd">public</span> Integer <span
 class="nf">getCustomToastLayout</span><span
 class="o">()</span> <span class="o">{</span><br><span
 class="nd">       </span> <span class="k">return</span> R<span
 class="o">.</span><span class="na">layout</span><span
 class="o">.</span><span class="na">my_custom_toast</span><span
 class="o">;</span><br><span class="nd">   </span> <span
 class="o">}</span><br> <br><span
 class="nd">   </span> <span class="nd">@Override</span><br><span
 class="nd">   </span> <span class="kd">public</span> Integer <span
 class="nf">getCustomAlertDialogLayout</span><span
 class="o">()</span> <span class="o">{</span><br><span
 class="nd">       </span> <span class="k">return</span> R<span
 class="o">.</span><span class="na">layout</span><span
 class="o">.</span><span class="na">my_custom_dialog</span><span
 class="o">;</span><br><span class="nd">   </span> <span
 class="o">}</span><br><br><span
 class="nd">   </span> <span class="nd">@Override</span><br><span
 class="nd">   </span> <span class="kd">public</span> Integer <span
 class="nf">getCustomAlertDialogLayoutOneButton</span><span
 class="o">()</span> <span class="o">{</span><br><span
 class="nd">   </span>     <span class="k">return</span> R<span
 class="o">.</span><span class="na">layout</span><span
 class="o">.</span><span class="na">my_custom_dialog_one_button</span><span
 class="o">;</span><br><span class="nd">   </span> <span
 class="o">}</span><br><span class="o">};</span><br><br><span
 class="nd">@Override</span><br><span
 class="kd">public</span> CustomNotificationsInterface <span
 class="nf">getCustomNotificationsLayouts</span><span
 class="o">()</span> <span class="o">{</span><br><span
 class="nd">   </span> <span class="k">return</span> customNotificationsInterface<span
 class="o">;</span><br><span class="o">}</span>
  </pre>
  </div>

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

     <div class="code">
  <pre><span class="kd">protected</span> <span
 class="kt">void</span> <span class="nf">showDialog</span><span
 class="o">(</span>String message<span class="o">,</span> View<span
 class="o">.</span><span class="na">OnClickListener</span> clickListenerPositiveButton<span
 class="o">);</span><br><span class="kd">protected</span> <span
 class="kt">void</span> <span class="nf">showDialogOneButton</span><span
 class="o">(</span>String message<span class="o">);</span><br>protected void showDialogOneButtonWithCustomAction(String message, View.OnClickListener positiveButtonClickListener);<br>protected void showDialogOneButtonWithoutAction(String message);<br><span
 class="kd">protected</span> <span class="kt">void</span> <span
 class="nf">buildCustomToast</span><span class="o">(</span>String message<span
 class="o">,</span> <span class="kt">int</span> toastLength<span
 class="o">);</span>
  </pre>
  </div>


     E.g.:

     <div class="code">
  <pre>View<span class="o">.</span><span
 class="na">OnClickListener</span> clickListenerPositiveButton <span
 class="o">=</span> <span class="k">new</span> View<span
 class="o">.</span><span class="na">OnClickListener</span><span
 class="o">()</span> <span class="o">{</span><br> <span
 class="nd">   @Override</span><br>    <span
 class="kd">public</span> <span class="kt">void</span> <span
 class="nf">onClick</span><span class="o">(</span>View v<span
 class="o">)</span> <span class="o">{</span><br>        <span
 class="c1">// Make the request again<br></span>        isUserRegisteredOnServer<span
 class="o">();</span><br>        VoucherSAPOConnect<span
 class="o">.</span><span class="na">this</span><span
 class="o">.</span><span class="na">alertDialog</span><span
 class="o">.</span><span class="na">dismiss</span><span
 class="o">();</span><br>    <span class="o">}</span><br><span
 class="o">};</span><br>showDialog<span
 class="o">(</span>errorString<span class="o">,</span> clickListenerPositiveButton<span
 class="o">);</span>
  </pre>
  </div>

     Or:

     <div class="code">
  <pre>showDialogOneButton<span class="o">(</span>checkUserServerMessage<span
 class="o">);</span>
  </pre>
  </div>

     Or:

     <div class="code">
  <pre>buildCustomToast<span class="o">(</span>getString<span
 class="o">(</span>R<span class="o">.</span><span
 class="na">string</span><span class="o">.</span><span
 class="na">sapoConnect_registrationIncomplete</span><span
 class="o">),</span> Toast<span class="o">.</span><span
 class="na">LENGTH_LONG</span><span class="o">);</span>
  </pre>
  </div>
