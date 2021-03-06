<h1>DSLR camera</h1>
<hr/>
<p>
	<h2>Why?</h2>
	<p>I am interested in astrophotography and I came across <a href="http://deepskystacker.free.fr">Deep Sky Stacker</a>, a free software that, given multiple pictures of the night sky, stacks them together in order to reduce noise and give a clearer picture (after some postprocessing).</p>
	<p>The basic idea of the application is to easily capture the images which can then be provided to DSS. Just strap your device on a stand, set your values and wait till it's done.</p>

</p>
<p>
	<h2>How?</h2>
	<p>The interface is minimal, with a menu on the left, zoom bar at the bottom and capture button.</p>
	<p>From the menu, set ISO speed, lock focus, auto exposure, white balance, exposure... Long press the capture button to enter how many pictures to take and press start. Once the pictures are taken, cover the camera <b>straight away</b> and, keeping the same settings, take some 10-20 dark frames. It is important to perform this step right after the light frames are taken in order to have the camera at the same temperature. Export all the frames to Deep Sky Stacker and let it do the magic.</p>
  


<a href="https://www.youtube.com/watch?v=e0JSTF8SGi4">Here's</a> a tutorial.

<h2>Now...</h2>
<p>...I am not expecting to get pictures of galaxies with a phone, but hopefully decent pictures of the night sky. When the British weather cooperates of course...</p>

<h2>Known Issues</h2>
<p>I am trying to implement RAW images format but:
<ul>
	<li>phone keeps freezing when calling Camera.takePicture(mShutterCallback, mPictureRAW, null)</li>
	<li>can't debug the Camera API as the phone runs 5.0.1 and 
Android Studio's SDK is 5.0.2, which gives the  <i>"Source code doesnt match the bytecode"</i> message</li>
<li>no raw format available on emulator's camera.</li>
</p>
