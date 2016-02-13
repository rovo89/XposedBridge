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
<link rel="shortcut icon" type="image/x-icon" href="<?cs var:toroot ?>assets/images/favicon.ico" />
<title><?cs
  if:page.title ?><?cs
    var:page.title ?> | <?cs
  /if ?>Xposed Framework API</title>

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

</head>
