<!DOCTYPE html>
<?cs include:"macros.cs" ?>
<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>
      Android Design<?cs if:page.title ?> - <?cs var:page.title ?><?cs /if ?>
    </title>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
    <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Roboto:regular,medium,thin,italic,mediumitalic">
    <link rel="stylesheet" href="<?cs var:toroot ?>assets/yui-3.3.0-reset-min.css">
    <link rel="stylesheet" href="<?cs var:toroot ?>assets/design/default.css">
    <script src="<?cs var:toroot ?>assets/jquery-1.6.2.min.js"></script>
    <script>var SITE_ROOT = '<?cs var:toroot ?>design';</script>
    <script src="<?cs var:toroot ?>assets/design/default.js"></script>
  </head>
  <body class="gc-documentation 
    <?cs if:(guide||develop||training||reference||tools||sdk) ?>develop<?cs
    elif:design ?>design<?cs
    elif:distribute ?>distribute<?cs
    /if ?>" itemscope itemtype="http://schema.org/Article">
    <a name="top"></a>

    <div id="page-container">

      <div id="page-header" itemscope itemtype="http://schema.org/WPHeader"><a href="<?cs var:toroot ?>design/index.html">Android Design</a></div>

      <div id="main-row">

        <div id="nav-container" itemscope itemtype="http://schema.org/SiteNavigationElement">

        <?cs call:design_nav() ?>

        </div>

        <div id="content">

<?cs if:header.hide ?>
<?cs else ?>
<div class="content-header <?cs if:header.justLinks ?>just-links<?cs /if ?>">
    <?cs if:header.justLinks ?>&nbsp;
      <?cs elif:header.title ?><h2><?cs var:header.title ?></h2>
                   <?cs else ?><h2><?cs var:page.title ?></h2>
    <?cs /if ?>
  <div class="paging-links" itemscope itemtype="http://schema.org/SiteNavigationElement">
    <a href="#" class="prev-page-link">Previous</a>
    <a href="#" class="next-page-link">Next</a>
  </div>
</div>
<?cs /if ?>

<?cs call:tag_list(root.descr) ?>

<?cs if:footer.hide ?>
<?cs else ?>
<div class="cols content-footer" itemscope itemtype="http://schema.org/SiteNavigationElement">
  <div class="paging-links col-9">&nbsp;</div>
  <div class="paging-links col-4">
    <a href="#" class="prev-page-link">Previous</a>
    <a href="#" class="next-page-link">Next</a>
  </div>
</div>
<?cs /if ?>

        </div>

      </div>

      <div id="page-footer" itemscope itemtype="http://schema.org/WPFooter">

        <p id="copyright">
          Except as noted, this content is licensed under
          <a href="http://creativecommons.org/licenses/by/2.5/">
          Creative Commons Attribution 2.5</a>.<br>
          For details and restrictions, see the
          <a href="http://developer.android.com/license.html">Content License</a>.
        </p>

        <p>
          <a href="http://www.android.com/terms.html">Site Terms of Service</a> &ndash;
          <a href="http://www.android.com/privacy.html">Privacy Policy</a> &ndash;
          <a href="http://www.android.com/branding.html">Brand Guidelines</a>
        </p>

      </div>
    </div>

    <script type="text/javascript">
    var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
    document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>
    <script type="text/javascript">
    var pageTracker = _gat._getTracker("UA-5831155-1");
    pageTracker._trackPageview();
    </script>

<!-- Start of Tag -->
<script type="text/javascript">
var axel = Math.random() + "";
var a = axel * 10000000000000;
document.write('<iframe src="https://2507573.fls.doubleclick.net/activityi;src=2507573;type=other026;cat=googl348;ord=' + a + '?" width="1" height="1" frameborder="0" style="display:none"></iframe>');
</script>
<noscript>
<iframe src="https://2507573.fls.doubleclick.net/activityi;src=2507573;type=other026;cat=googl348;ord=1?" width="1" height="1" frameborder="0" style="display:none"></iframe>
</noscript>
<!-- End of Tag -->
  </body>
</html>
