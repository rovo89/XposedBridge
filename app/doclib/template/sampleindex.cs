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
<?cs if:projectStructure ?>
<a href="<?cs var:toroot ?>samples/<?cs var:projectDir ?>/index.html">Overview</a>
&#124; Project<?cs else ?>Overview
&#124; <a href="<?cs var:toroot ?>samples/<?cs var:projectDir ?>/project.html">Project</a>
<?cs /if ?>
&#124; <a href="<?cs var:toroot ?>downloads/samples/<?cs var:projectDir ?>.zip"
    onclick="ga('send', 'event', 'Samples', 'Download', <?cs var:projectDir ?>);"
    >Download</a>

</div><!-- end sum-details-links -->

</div><!-- end breadcurmb block -->

<h1 itemprop="name"><?cs var:projectDir ?></h1>
  
<div id="jd-content">
<?cs def:display_files(files) ?>

    <?cs each:file = files ?>
        <?cs if:file.Type != "dir" ?>
            <div class="structure-<?cs var:file.Type ?>">
            <a href="<?cs var:toroot ?><?cs var:file.Href ?>"><?cs var:file.Name ?></a>
            </div>
        <?cs else ?>
            <div class="toggle-content opened structure-dir">
               <a href="#" onclick="return toggleContent(this)">
               <img src="<?cs var:toroot ?>assets/images/triangle-opened.png"
                  class="toggle-content-img structure-toggle-img" height="9px" width="9px" />
               <?cs var:file.Name ?></a><?cs 
                  if:file.SummaryFlag == "true" ?><span class="dirInfo"
                    >[&nbsp;<a href="file.SummaryHref">Info</a>&nbsp;]</a></span><?cs 
                  /if ?>
               <div class="toggle-content-toggleme structure-toggleme"> 
            <?cs if:file.Sub.0.Name ?>
                 <?cs call:display_files(file.Sub) ?>
            <?cs /if ?>
               </div> <?cs # /toggleme ?>
            </div> <?cs # /toggle-content ?>
         <?cs /if ?>
    <?cs /each ?>
<?cs /def ?>

<?cs if:android.whichdoc == "online" ?>
  <?cs # If this is the online docs, build the src code navigation links ?>

  <?cs if:projectStructure ?>

    <?cs call:display_files(Files) ?>

  <?cs else ?> <?cs # else not project structure doc ?>

    <?cs var:summary ?>

    <?cs # Remove project structure from landing pages for now
         # <h2>Project Structure</h2>
         # <p>Decide what to do with this ...</p>
         # <?cs call:display_files(Files) ?>

  <?cs /if ?> <?cs # end if projectStructure ?>

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


