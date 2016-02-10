<head>
<?cs
  ####### If building devsite, add some meta data needed for when generating the top nav ######### ?>
<?cs
  if:devsite ?><?cs
    if:guide||develop||training||reference||tools||sdk||google||samples
      ?><meta name="top_category" value="develop" /><?cs
    elif:google
      ?><meta name="top_category" value="google" /><?cs
    elif:reference && !(reference.gms || reference.gcm)
      ?><meta name="top_category" value="css-fullscreen" /><?cs
    /if ?>
  <?cs
  /if
?><?cs
  # END if/else devsite ?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
<meta content="IE=edge" http-equiv="X-UA-Compatible">
<?cs
  if:page.metaDescription ?>
<meta name="Description" content="<?cs var:page.metaDescription ?>"><?cs
  /if ?>
<link rel="shortcut icon" type="image/x-icon" href="<?cs var:toroot ?>favicon.ico" />
<title><?cs
  if:page.title ?><?cs
    var:page.title ?> | <?cs
  /if ?>Android Developers</title>

<!-- STYLESHEETS -->
<link rel="stylesheet"
href="<?cs
if:android.whichdoc != 'online' ?>http:<?cs
/if ?>//fonts.googleapis.com/css?family=Roboto+Condensed">
<link rel="stylesheet" href="<?cs
if:android.whichdoc != 'online' ?>http:<?cs
/if ?>//fonts.googleapis.com/css?family=Roboto:light,regular,medium,thin,italic,mediumitalic,bold"
  title="roboto">
<?cs 
  if:ndk ?><link rel="stylesheet" href="<?cs
  if:android.whichdoc != 'online' ?>http:<?cs
  /if ?>//fonts.googleapis.com/css?family=Roboto+Mono:400,500,700" title="roboto-mono" type="text/css"><?cs
/if ?>
<link href="<?cs var:toroot ?>assets/css/default.css?v=7" rel="stylesheet" type="text/css">

<?cs if:reference && !(reference.gms || reference.gcm || preview) ?>
<!-- FULLSCREEN STYLESHEET -->
<link href="<?cs var:toroot ?>assets/css/fullscreen.css" rel="stylesheet" class="fullscreen"
type="text/css">
<?cs /if ?>

<!-- JAVASCRIPT -->
<script src="<?cs if:android.whichdoc != 'online' ?>http:<?cs /if ?>//www.google.com/jsapi" type="text/javascript"></script>
<?cs
if:devsite
  ?><script src="<?cs var:toroot ?>_static/js/android_3p-bundle.js" type="text/javascript"></script><?cs
else
  ?><script src="<?cs var:toroot ?>assets/js/android_3p-bundle.js" type="text/javascript"></script><?cs
/if ?><?cs
  if:page.customHeadTag ?>
<?cs var:page.customHeadTag ?><?cs
  /if ?>
<script type="text/javascript">
  var toRoot = "<?cs var:toroot ?>";
  var metaTags = [<?cs var:meta.tags ?>];
  var devsite = <?cs if:devsite ?>true<?cs else ?>false<?cs /if ?>;
</script>
<script src="<?cs var:toroot ?>assets/js/docs.js?v=6" type="text/javascript"></script>

<?cs if:helpoutsWidget ?>
<script type="text/javascript" src="https://helpouts.google.com/ps/res/embed.js" defer async
    data-helpouts-embed data-helpouts-vertical="programming"
    data-helpouts-tags="<?cs var:page.tags ?>" data-helpouts-prefix="android"
    data-helpouts-standalone="true"></script>
<?cs /if ?>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-5831155-1', 'android.com');
  ga('create', 'UA-49880327-2', 'android.com', {'name': 'universal'});  // New tracker);
  ga('send', 'pageview');
  ga('universal.send', 'pageview'); // Send page view for new tracker.
</script>

</head>
