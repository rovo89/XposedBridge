<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<html<?cs if:devsite ?> devsite<?cs /if ?>>
<?cs if:sdk.redirect ?>
  <head>
    <title>Redirecting...</title>
    <meta http-equiv="refresh" content="0;url=<?cs var:toroot ?>sdk/<?cs
      if:sdk.redirect.path ?><?cs var:sdk.redirect.path ?><?cs
      else ?>index.html<?cs /if ?>">
  </head>
<?cs else ?>
  <?cs include:"head_tag.cs" ?>
<?cs /if ?>
<body class="gc-documentation
  <?cs if:(guide||develop||training||reference||tools||sdk) ?>develop<?cs
  elif:design ?>design<?cs
  elif:distribute ?>distribute<?cs
  /if ?>" itemscope itemtype="http://schema.org/CreativeWork">
  <a name="top"></a>
<?cs include:"header.cs" ?>


<div <?cs if:fullpage
?><?cs else
?>class="col-13" id="doc-col"<?cs /if ?> >

<?cs if:sdk.redirect ?>

<div class="g-unit">
  <div id="jd-content">
    <p>Redirecting to
    <a href="<?cs var:toroot ?>sdk/<?cs
      if:sdk.redirect.path ?><?cs var:sdk.redirect.path ?><?cs
      else ?>index.html<?cs /if ?>"><?cs
      if:sdk.redirect.path ?><?cs var:sdk.redirect.path ?><?cs
      else ?>Download the SDK<?cs /if ?>
    </a> ...</p>

<?cs else ?>
<?cs # else, if NOT redirect ...
#
#
# The following is for SDK/NDK pages
#
#
?>

<?cs if:header.hide ?><?cs else ?>
<h1 itemprop="name"><?cs var:page.title ?></h1>
<?cs /if ?>
  <div id="jd-content" itemprop="description">

<?cs if:sdk.not_latest_version ?>
  <div class="special">
    <p><strong>This is NOT the current Android SDK release.</strong></p>
    <p><a href="/sdk/index.html">Download the current Android SDK</a></p>
  </div>
<?cs /if ?>


<?cs if:ndk ?>
<?cs #
#
#
#
#
#
#
# the following is for the NDK
#
# (nested in if/else redirect)
#
#
#
#
?>

  <table class="download" id="download-table">
    <tr>
      <th>Platform</th>
      <th>Package</th>
      <th style="white-space:nowrap">Size (Bytes)</th>
      <th>MD5 Checksum</th>
  </tr>
  <tr>
    <td>Windows 32-bit</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.win32_download ?>"><?cs var:ndk.win32_download ?></a>
    </td>
    <td><?cs var:ndk.win32_bytes ?></td>
    <td><?cs var:ndk.win32_checksum ?></td>
  </tr>
 <!-- <tr>
   <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.win32.legacy_download ?>"><?cs var:ndk.win32.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.win32.legacy_bytes ?></td>
    <td><?cs var:ndk.win32.legacy_checksum ?></td>
  </tr> -->
  <tr>
    <td>Windows 64-bit</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.win64_download ?>"><?cs var:ndk.win64_download ?></a>
    </td>
    <td><?cs var:ndk.win64_bytes ?></td>
    <td><?cs var:ndk.win64_checksum ?></td>
  </tr>
 <!--  <tr>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.win64.legacy_download ?>"><?cs var:ndk.win64.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.win64.legacy_bytes ?></td>
    <td><?cs var:ndk.win64.legacy_checksum ?></td>
  </tr> -->
<!--   (this item is deprecated)
  <tr>
    <td>Mac OS X 32-bit</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.mac32_download ?>"><?cs var:ndk.mac32_download ?></a>
    </td>
    <td><?cs var:ndk.mac32_bytes ?></td>
    <td><?cs var:ndk.mac32_checksum ?></td>
  </tr> -->
 <!-- (this item is deprecated)
  <tr>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.mac32.legacy_download ?>"><?cs var:ndk.mac32.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.mac32.legacy_bytes ?></td>
    <td><?cs var:ndk.mac32.legacy_checksum ?></td>
  </tr> -->
    <td>Mac OS X 64-bit</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.mac64_download ?>"><?cs var:ndk.mac64_download ?></a>
    </td>
    <td><?cs var:ndk.mac64_bytes ?></td>
    <td><?cs var:ndk.mac64_checksum ?></td>
  </tr>
 <!--  <tr>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.mac64.legacy_download ?>"><?cs var:ndk.mac64.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.mac64.legacy_bytes ?></td>
    <td><?cs var:ndk.mac64.legacy_checksum ?></td>
  </tr> -->
  <tr>
    <td>Linux 32-bit (x86)</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.linux32_download ?>"><?cs var:ndk.linux32_download ?></a>
    </td>
    <td><?cs var:ndk.linux32_bytes ?></td>
    <td><?cs var:ndk.linux32_checksum ?></td>
  </tr>
 <!--  <tr>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.linux32.legacy_download ?>"><?cs var:ndk.linux32.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.linux32.legacy_bytes ?></td>
    <td><?cs var:ndk.linux32.legacy_checksum ?></td>
  </tr> -->
  <tr>
    <td>Linux 64-bit (x86)</td>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.linux64_download ?>"><?cs var:ndk.linux64_download ?></a>
    </td>
    <td><?cs var:ndk.linux64_bytes ?></td>
    <td><?cs var:ndk.linux64_checksum ?></td>
  </tr>
  <!--  <tr>
    <td>
  <a onClick="return onDownload(this)"
     href="http://dl.google.com/android/ndk/<?cs var:ndk.linux64.legacy_download ?>"><?cs var:ndk.linux64.legacy_download ?></a>
    </td>
    <td><?cs var:ndk.linux64.legacy_bytes ?></td>
    <td><?cs var:ndk.linux64.legacy_checksum ?></td>
  </tr> -->

  </table>

  <?cs ########  HERE IS THE JD DOC CONTENT ######### ?>
  <?cs call:tag_list(root.descr) ?>



<script>
  function onDownload(link) {

    $("#downloadForRealz").html("Download " + $(link).text());
    $("#downloadForRealz").attr('href',$(link).attr('href'));

    $("#tos").fadeIn('slow');

    location.hash = "download";
    return false;
  }


  function onAgreeChecked() {
    if ($("input#agree").is(":checked")) {
      $("a#downloadForRealz").removeClass('disabled');
    } else {
      $("a#downloadForRealz").addClass('disabled');
    }
  }

  function onDownloadNdkForRealz(link) {
    if ($("input#agree").is(':checked')) {
      $("#tos").fadeOut('slow');

      $('html, body').animate({
          scrollTop: $("#Installing").offset().top
        }, 800, function() {
          $("#Installing").click();
      });

      return true;
    } else {
      $("label#agreeLabel").parent().stop().animate({color: "#258AAF"}, 200,
        function() {$("label#agreeLabel").parent().stop().animate({color: "#222"}, 200)}
      );
      return false;
    }
  }

  $(window).hashchange( function(){
    if (location.hash == "") {
      location.reload();
    }
  });

</script>

  <?cs else ?>
<?cs # end if NDK ...
#
#
#
#
#
#
# the following is for the SDK
#
# (nested in if/else redirect and if/else NDK)
#
#
#
#
?>
  <?cs if:android.whichdoc == "online" ?>


<?cs ########  HERE IS THE JD DOC CONTENT FOR ONLINE ######### ?>
<?cs call:tag_list(root.descr) ?>




<div class="pax col-13 online" style="margin:0;">


<h3>SDK Tools Only</h3>

<p>If you prefer to use a different IDE or run the tools from the
command line or with build scripts, you can instead download the stand-alone Android SDK Tools.
These packages provide the basic SDK tools for app development, without an IDE.
Also see the <a href="<?cs var:toroot ?>tools/sdk/tools-notes.html">SDK tools release notes</a>.</p>

  <table class="download">
    <tr>
      <th>Platform</th>
      <th>Package</th>
      <th>Size</th>
      <th>SHA-1 Checksum</th>
  </tr>
  <tr>
    <td rowspan="2">Windows</td>
    <td>
  <a onclick="return onDownload(this)" id="win-tools" href="http://dl.google.com/android/<?cs
var:sdk.win_installer
?>"><?cs var:sdk.win_installer ?></a> (Recommended)
    </td>
    <td><?cs var:sdk.win_installer_bytes ?> bytes</td>
    <td><?cs var:sdk.win_installer_checksum ?></td>
  </tr>
  <tr>
    <!-- blank TD from Windows rowspan -->
    <td>
  <a onclick="return onDownload(this)" href="http://dl.google.com/android/<?cs var:sdk.win_download
?>"><?cs var:sdk.win_download ?></a>
    </td>
    <td><?cs var:sdk.win_bytes ?> bytes</td>
    <td><?cs var:sdk.win_checksum ?></td>
  </tr>
  <tr>
    <td><nobr>Mac OS X</nobr></td>
    <td>
  <a onclick="return onDownload(this)" id="mac-tools" href="http://dl.google.com/android/<?cs
var:sdk.mac_download
?>"><?cs var:sdk.mac_download ?></a>
    </td>
    <td><?cs var:sdk.mac_bytes ?> bytes</td>
    <td><?cs var:sdk.mac_checksum ?></td>
  </tr>
  <tr>
    <td>Linux</td>
    <td>
  <a onclick="return onDownload(this)" id="linux-tools" href="http://dl.google.com/android/<?cs
var:sdk.linux_download
?>"><?cs var:sdk.linux_download ?></a>
    </td>
    <td><?cs var:sdk.linux_bytes ?> bytes</td>
    <td><?cs var:sdk.linux_checksum ?></td>
  </tr>
  </table>



<h3>All Android Studio Packages</h3>

<p>Select a specific Android Studio package for your platform. Also see the
<a href="<?cs var:toroot ?>tools/revisions/studio.html">Android Studio release notes</a>.</p>

  <table class="download">
    <tr>
      <th>Platform</th>
      <th>Package</th>
      <th>Size</th>
      <th>SHA-1 Checksum</th>
  </tr>

  <tr>
    <td rowspan="3">Windows</td>
    <td>
  <a onclick="return onDownload(this,false,true)" id="win-bundle"
    href="https://dl.google.com/dl/android/studio/install/<?cs var:studio.version ?>/<?cs var:studio.win_bundle_exe_download ?>"
    ><?cs var:studio.win_bundle_exe_download ?></a><br>(Recommended)
    </td>
    <td><?cs var:studio.win_bundle_exe_bytes ?> bytes</td>
    <td><?cs var:studio.win_bundle_exe_checksum ?></td>
  </tr>

  <tr>
    <!-- blank TD from Windows rowspan -->
    <td>
  <a onclick="return onDownload(this,false,true)" id="win-bundle-notools"
    href="https://dl.google.com/dl/android/studio/install/<?cs var:studio.version ?>/<?cs var:studio.win_notools_exe_download ?>"
    ><?cs var:studio.win_notools_exe_download ?></a><br>(No SDK tools included)
    </td>
    <td><?cs var:studio.win_notools_exe_bytes ?> bytes</td>
    <td><?cs var:studio.win_notools_exe_checksum ?></td>
  </tr>

  <tr>
    <!-- blank TD from Windows rowspan -->
    <td>
  <a onclick="return onDownload(this,false,true)" id="win-bundle-zip"
    href="https://dl.google.com/dl/android/studio/ide-zips/<?cs var:studio.version ?>/<?cs var:studio.win_bundle_download ?>"
    ><?cs var:studio.win_bundle_download ?></a>
    </td>
    <td><?cs var:studio.win_bundle_bytes ?> bytes</td>
    <td><?cs var:studio.win_bundle_checksum ?></td>
  </tr>

  <tr>
    <td><nobr>Mac OS X</nobr></td>
    <td>
  <a onclick="return onDownload(this,false,true)" id="mac-bundle"
    href="https://dl.google.com/dl/android/studio/install/<?cs var:studio.version ?>/<?cs var:studio.mac_bundle_download ?>"
    ><?cs var:studio.mac_bundle_download ?></a>
    </td>
    <td><?cs var:studio.mac_bundle_bytes ?> bytes</td>
    <td><?cs var:studio.mac_bundle_checksum ?></td>
  </tr>

  <tr>
    <td>Linux</td>
    <td>
  <a onclick="return onDownload(this,false,true)" id="linux-bundle"
    href="https://dl.google.com/dl/android/studio/ide-zips/<?cs var:studio.version ?>/<?cs var:studio.linux_bundle_download ?>"
    ><?cs var:studio.linux_bundle_download ?></a>
    </td>
    <td><?cs var:studio.linux_bundle_bytes ?> bytes</td>
    <td><?cs var:studio.linux_bundle_checksum ?></td>
  </tr>
  </table>



</div><!-- end pax -->





<script>

  if (location.hash == "#Requirements") {
    $('.reqs').show();
  } else if (location.hash == "#ExistingIDE") {
	 $('.ide').show();
  }

  var os;
  var bundlename;
  var $toolslink;

  if (navigator.appVersion.indexOf("Mobile")!=-1) {
    // Do nothing for any "mobile" user agent
  } else if (navigator.appVersion.indexOf("Win")!=-1) {
    os = "Windows";
    bundlename = '#win-bundle';
    $toolslink = $('#win-tools');
  } else if (navigator.appVersion.indexOf("Mac")!=-1) {
    os = "Mac";
    bundlename = '#mac-bundle';
    $toolslink = $('#mac-tools');
  } else if (navigator.appVersion.indexOf("Linux")!=-1 && navigator.appVersion.indexOf("Android")==-1) {
    os = "Linux";
    bundlename = '#linux-bundle';
    $toolslink = $('#linux-tools');
  }

  if (os != undefined) {
    $('#not-supported').hide();

    /* set up primary Android Studio download button */
    $('.download-bundle-button > .small').html(" for " + os);
    $('.download-bundle-button').click(function() {return onDownload(this,true,true);}).attr('href', bundlename);
  }


  function onDownload(link, button, bundle) {

    /* set text for download button */
    if (button) {
      $("#downloadForRealz").html($(link).text());
    } else {
      $("#downloadForRealz").html("Download " + $(link).text());
    }

    $("#downloadForRealz").attr('bundle', bundle);
    if (bundle && !button) {
      $("a#downloadForRealz").attr("name", "#" + $(link).attr('id'));
    } else {
      $("a#downloadForRealz").attr("name", $(link).attr('href'));
    }

    $("#tos").show();
    $("#landing").hide();

    location.hash = "top";
    return false;
  }


  function onAgreeChecked() {
    /* verify that the TOS is agreed */
    if ($("input#agree").is(":checked")) {

      /* if downloading the bundle */
      if ($("#downloadForRealz").attr('bundle')) {
        /* construct the name of the link we want */
        linkId = $("a#downloadForRealz").attr("name");
        /* set the real url for download */
        $("a#downloadForRealz").attr("href", $(linkId).attr("href"));
      } else {
        $("a#downloadForRealz").attr("href", $("a#downloadForRealz").attr("name"));
      }

      /* reveal the download button */
      $("a#downloadForRealz").removeClass('disabled');
    } else {
      $("a#downloadForRealz").addClass('disabled');
    }
  }

  function onDownloadForRealz(link) {
    if ($("input#agree").is(':checked')) {
      location.hash = "";
      location.hash = "top";
      $("div.sdk-terms").slideUp();
      $("h1#tos-header").text('Now downloading...');
      $(".sdk-terms-intro").text('You\'ll be redirected to the install instructions in a moment.');
      $("#sdk-terms-form").fadeOut('slow', function() {
        setTimeout(function() {
          if ($("#downloadForRealz").attr('bundle') == 'true') {
            // User downloaded the studio Bundle
            window.location = "/sdk/installing/index.html?pkg=studio";
          } else {
            // User downloaded the SDK Tools
            window.location = "/sdk/installing/index.html?pkg=tools";
          }
        }, 3000);
      });
      ga('send', 'event', 'SDK', 'IDE and Tools', $("#downloadForRealz").html());
      return true;
    } else {
      return false;
    }
  }

  $(window).hashchange( function(){
    if (location.hash == "") {
      location.reload();
    }
  });

</script>



</div><!-- end the wrapper used for relative/absolute positions  -->
<?cs # THIS DIV WAS OPENED IN INDEX.JD ?>




  <?cs else ?> <?cs # end if online ?>

    <?cs if:sdk.preview ?><?cs # it's preview offline docs ?>
      <p>Welcome developers! We are pleased to provide you with a preview SDK for the upcoming
    Android 3.0 release, to give you a head-start on developing applications for it.
    </p>

      <p>See the <a
    href="<?cs var:toroot ?>sdk/preview/start.html">Getting Started</a> document for more information
    about how to set up the preview SDK and get started.</p>
    <style type="text/css">
    .non-preview { display:none; }
    </style>

    <?cs else ?><?cs # it's normal offline docs ?>

      <?cs ########  HERE IS THE JD DOC CONTENT FOR OFFLINE ######### ?>
      <?cs call:tag_list(root.descr) ?>
      <style type="text/css">
        body .offline { display:block; }
        body .online { display:none; }
      </style>
      <script>
        $('.reqs').show();
      </script>
    <?cs /if ?>

  <?cs /if ?> <?cs # end if/else online ?>

<?cs /if ?> <?cs # end if/else NDK ?>

<?cs /if ?> <?cs # end if/else redirect ?>


</div><!-- end jd-content -->

<?cs if:!sdk.redirect ?>
<?cs include:"footer.cs" ?>
<?cs /if ?>

</div><!-- end g-unit -->

<?cs include:"trailer.cs" ?>

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



