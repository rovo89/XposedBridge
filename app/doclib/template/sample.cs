<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<html<?cs if:devsite ?> devsite<?cs /if ?>>
<?cs include:"head_tag.cs" ?>
<body class="gc-documentation develop samples" itemscope itemtype="http://schema.org/Article">
<?cs include:"header.cs" ?>

<div <?cs if:fullpage
?>class="fullpage"<?cs elif:design||tools||about||sdk||distribute
?>class="col-13" id="doc-col"<?cs else
?>class="col-12" id="doc-col"<?cs /if ?> >

<!-- start breadcrumb block -->
<div id="api-info-block">
  <div class="sum-details-links">

  <!-- related links -->
  <a href="<?cs var:toroot ?>samples/<?cs var:projectDir ?>/index.html">Overview</a>
  &#124; <a href="<?cs var:toroot ?>samples/<?cs var:projectDir ?>/project.html">Project</a>
  &#124; <a href="<?cs var:toroot ?>downloads/samples/<?cs var:projectDir ?>.zip"
    onclick="ga('send', 'event', 'Samples', 'Download', <?cs var:projectDir ?>);"
    >Download</a>

</div><!-- end sum-details-links -->

</div><!-- end breadcurmb block -->

<div id="jd-header" style="border:0;">

<div id="pathCrumb">
<?cs each:item = parentdirs ?>
  <?cs if:LinkifyPathCrumb
    ?><a href="<?cs var:toroot ?><?cs var:item.Link ?>"><?cs var:item.Name ?></a> / 
  <?cs else
    ?><?cs var:item.Name ?> / <?cs /if ?>
<?cs /each ?>
</div>

  <h1 itemprop="name"><?cs var:page.title ?></h1>
</div>
<!-- end breadcrumb block -->


<?cs # THIS IS THE MAIN DOC CONTENT ?>
<div id="jd-content">

<?cs if:android.whichdoc == "online" ?>

<?cs # If this is the online docs, build the src code navigation links ?>


<?cs var:summary ?>

<!-- begin file contents -->

<?cs # embed image/videos if below maxsize (show message otherwise), else display source code ?>
<?cs if:resType == "img" ?>
  <div id="codesample-resource"
    <?cs if:noDisplay ?>
      class="noDisplay"><div class="noDisplay-message"></div>
    <?cs else ?>
      ><img src="<?cs var:realFile ?>" title="<?cs var:page.title ?>">
    <?cs /if ?>
  </div>
<?cs elif:resType == "video" ?>
  <div id="codesample-resource"
    <?cs if:noDisplay ?>
      class="noDisplay"><div class="noDisplay-message"></div>
    <?cs else ?>
      ><video class="play-on-hover" controls style="border:1px solid #ececec;background-color:#f9f9f9;" poster="">
        <source src="<?cs var:page.title ?>">
      </video>
    <?cs /if ?>
  </div>
<?cs else ?>
  <div id="codesample-wrapper">
    <pre id="codesample-line-numbers" class="no-pretty-print hidden"></pre>
    <pre id="codesample-block"><?cs var:fileContents ?></pre>
  </div>
  <script type="text/javascript">
  initCodeLineNumbers();
  </script>
<?cs /if ?>

<!-- end file contents -->

<?cs else ?><?cs
  # else, this means it's offline docs,
          so don't show src links (we dont have the pages!) ?>

<?cs /if ?><?cs # end if/else online docs ?>

      <div class="content-footer <?cs
                    if:fullpage ?>wrap<?cs
                    else ?>cols<?cs /if ?>"
                    itemscope itemtype="http://schema.org/SiteNavigationElement">
        <div class="<?cs
                    if:fullpage ?>col-16<?cs
                    elif:training||guide ?>col-8<?cs
                    else ?>col-9<?cs /if ?>" style="padding-top:4px">
          <?cs if:!page.noplus ?><?cs if:fullpage ?><style>#___plusone_0 {float:right !important;}</style><?cs /if ?>
            <div class="g-plusone" data-size="medium"></div>
          <?cs /if ?>
        </div>
        <?cs if:!fullscreen ?>
        <div class="paging-links col-4">
          <?cs if:(design||training||walkthru) && !page.landing && !page.trainingcourse && !footer.hide ?>
            <a href="#" class="prev-page-link hide"
                zh-tw-lang="上一堂課"
                zh-cn-lang="上一课"
                ru-lang="Предыдущий"
                ko-lang="이전"
                ja-lang="前へ"
                es-lang="Anterior"
                >Previous</a>
            <a href="#" class="next-page-link hide"
                zh-tw-lang="下一堂課"
                zh-cn-lang="下一课"
                ru-lang="Следующий"
                ko-lang="다음"
                ja-lang="次へ"
                es-lang="Siguiente"
                >Next</a>
          <?cs /if ?>
        </div>
        <?cs /if ?>
      </div>

      <?cs # for training classes, provide a different kind of link when the next page is a different class ?>
      <?cs if:training && !page.article ?>
      <div class="content-footer next-class" style="display:none" itemscope itemtype="http://schema.org/SiteNavigationElement">
          <a href="#" class="next-class-link hide">Next class: </a>
      </div>
      <?cs /if ?>

  </div> <!-- end jd-content -->

<?cs include:"footer.cs" ?>
</div><!-- end doc-content -->

<?cs include:"trailer.cs" ?>

</body>
</html>







