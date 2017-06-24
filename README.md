<h1>DSLR camera</h1>
<hr/>
<p>
	<h2>Why?</h2>
	<p>I like astrophotography and I came across this free software <a href="http://deepskystacker.free.fr">Deep Sky Stacker</a> which takes hundreds of pictures of the night sky and stacks them toghether in order to reduce noise and give a clearer picture (after some postprocessing).</p>
	<p>The basic idea is to strap your device on a stand, set some values and wait till it's done.</p>

</p>
<p>
	<h2>How?</h2>
	<p>The interface is minimal, with a menu on the left, zoom bar in the bottom and capture button.</p>
	<p>From the menu, set ISO speed, lock focus, auto exposure, white balance, exposure... Long press the capture button to enter how many pictures to take and press start. Right after, cover the camera and, keeping the same settings, take some 10/20 dark frames. Export all the frames on Deep Sky Staker and let it do the magic.</p>
  


<a href="https://www.youtube.com/watch?v=e0JSTF8SGi4">Here's</a> a tutorial.

<h2>Now...</h2>
<p>...I am not expecting to get pictures of galaxies with a phone, but hopefully decent pics of the night sky. When the British weather cooperates of course...</p>

<h2>Known Issues</h2>
<p>I am trying to implement RAW images format but my phone keeps freezing when calling 
Camera.takePicture(mShutterCallback, mPictureRAW, null) and can't debug the Camera API as the phone runs on 5.0.1 and 
Android Studio's SDK is 5.0.2 which gives the  <i>"Source code doesnt match the bytecode"</i> message, 
and no raw available on virtual device's camera.</p>
