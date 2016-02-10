<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<html<?cs if:devsite ?> devsite<?cs /if ?>>
<?cs include:"head_tag.cs" ?>
<body class="gc-documentation

<?cs # add document classes for navigation header selection (and other stuff) ?>
<?cs
  if:(google || reference.gms || reference.gcm) ?>google <?cs /if ?><?cs
  if:ndk ?>ndk<?cs
    if:guide ?> guide<?cs /if ?><?cs
    if:samples ?> samples<?cs /if ?><?cs
    if:reference ?> reference<?cs /if ?><?cs
    if:downloads ?> downloads<?cs /if ?><?cs
  else ?><?cs
    if:(guide||develop||training||reference||tools||sdk||google||samples||preview) ?>develop<?cs
      if:guide ?> guide<?cs /if ?><?cs
      if:samples ?> samples<?cs /if ?><?cs
      if:preview ?> preview<?cs /if ?><?cs
    elif:(distribute||googleplay||essentials||users||engage||monetize||disttools||stories||analyze) ?>distribute<?cs
      if:googleplay ?> googleplay<?cs /if ?><?cs
      if:essentials ?> essentials<?cs /if ?><?cs
      if:users ?> users<?cs /if ?><?cs
      if:engage ?> engage<?cs /if ?><?cs
      if:monetize ?> monetize<?cs /if ?><?cs
      if:disttools ?> disttools<?cs /if ?><?cs
      if:stories ?> stories<?cs /if ?><?cs
      if:analyze ?> analyze<?cs /if ?><?cs
    elif:(about||wear||tv||auto) ?>about<?cs
    elif:design ?>design<?cs
    /if ?><?cs
    if:page.trainingcourse ?> trainingcourse<?cs /if ?><?cs
/if ?>" itemscope itemtype="http://schema.org/Article"><?cs
include:"header.cs" ?>

<div <?cs
  if:fullpage
    ?>class="fullpage"<?cs
  elif:(design||tools||about||sdk||googleplay||essentials||users||engage||monetize||disttools||stories) && !nonavpage
    ?>class="col-13" id="doc-col"<?cs
  elif:!nonavpage
    ?>class="col-12" id="doc-col"<?cs /if ?> >

<?cs if:(design||training||walkthru) && !page.trainingcourse && !page.article ?><?cs # header logic for docs that provide previous/next buttons ?>
  <?cs if:header.hide ?>
  <?cs else ?>
  <div class="content-header <?cs if:header.justLinks ?>just-links<?cs /if ?>">
      <?cs if:header.justLinks ?>&nbsp;
      <?cs else ?><h1 itemprop="name"><?cs var:page.title ?></h1>
      <?cs /if ?>
    <?cs if:training ?>
      <div class="training-nav-top" itemscope itemtype="http://schema.org/SiteNavigationElement">
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
        <a href="#" class="start-class-link hide"
            zh-tw-lang="開始上課"
            zh-cn-lang="开始"
            ru-lang="Начало работы"
            ko-lang="시작하기"
            ja-lang="開始する"
            es-lang="Empezar"
            >Get started</a>
      </div>
    <?cs elif:!page.trainingcourse ?>
      <div class="paging-links" itemscope itemtype="http://schema.org/SiteNavigationElement">
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
      </div>
    <?cs /if ?><?cs # end if training ?>
  </div>
  <?cs /if ?><?cs # end if header.hide ?>

<?cs elif:samplesProjectIndex ?>
  <div id="api-info-block">
  <div class="sum-details-links">
  Overview
  &#124; <a href="<?cs var:toroot ?>samples/<?cs var:projectDir ?>/project.html">Project</a>
  &#124; <a href="<?cs var:toroot ?>downloads/samples/<?cs var:projectDir ?>.zip">Download</a>
  </div><!-- end sum-details-links -->
  </div><!-- end breadcurmb block -->
  <h1 itemprop="name"><?cs var:projectDir ?></h1>

<?cs else ?>
  <?cs if:training ?>
<?cs # horrible horrible hack to move TOC up when the next/prev links are not there ?>
<style>
  #tb-wrapper {
    margin-top:6px;
  }
</style>
  <?cs /if ?>

  <?cs if:(!fullpage && !header.hide) ?>
    <?cs if:page.landing ?><?cs # header logic for docs that are landing pages ?>
      <div class="landing-banner">
        <?cs if:page.landing.image ?><?cs # use two-column layout only if there is an image ?>
        <div class="cols">
          <div class="col-6">
            <img src="<?cs var:toroot ?><?cs var:page.landing.image ?>" alt="" />
          </div>
          <div class="col-6">
        <?cs /if ?>
          <h1 itemprop="name" style="margin-bottom:0;"><?cs var:page.title ?></h1>
          <p itemprop="description"><?cs var:page.landing.intro ?></p>

          <p><a class="next-page-link topic-start-link"></a></p>
        <?cs if:page.landing.image ?>
          </div>
        </div>
        <?cs /if ?>
      </div>
    <?cs else ?>
      <?cs if:tab1 ?><div id="title-tabs-wrapper"><?cs /if ?>
        <h1 itemprop="name" <?cs if:tab1 ?>class="with-title-tabs"<?cs /if ?>><?cs var:page.title ?></h1><?cs
          if:tab1 ?><ul id="title-tabs">
              <li class="selected"><a href="<?cs var:tab1.link ?>"><?cs var:tab1 ?></a></li>
              <?cs if:tab2 ?>
              <li><a href="<?cs var:tab2.link ?>"><?cs var:tab2 ?></a></li><?cs /if ?>
              <?cs if:tab3 ?>
              <li><a href="<?cs var:tab3.link ?>"><?cs var:tab3 ?></a></li><?cs /if ?>
            </ul>
          <?cs /if ?>
      <?cs if:tab1 ?></div><!-- end tab-wrapper --><?cs /if ?>
    <?cs /if ?>
  <?cs /if ?>
<?cs /if ?><?cs # end if design ?>

  <?cs # THIS IS THE MAIN DOC CONTENT ?>
  <div id="jd-content">


    <div class="jd-descr" itemprop="articleBody">
    <?cs call:tag_list(root.descr) ?>
    </div>

    <?cs if:!fullscreen && (design||training||walkthru) && !page.landing && !page.trainingcourse && !footer.hide ?>
      <div class="content-footer <?cs
                    if:fullpage ?>wrap<?cs /if ?>"
                    itemscope itemtype="http://schema.org/SiteNavigationElement">
          <div class="paging-links">
            <a href="#" class="next-page-link hide"
                zh-tw-lang="下一堂課"
                zh-cn-lang="下一课"
                ru-lang="Следующий"
                ko-lang="다음"
                ja-lang="次へ"
                es-lang="Siguiente"
                >Next</a>
            <a href="#" class="start-class-link hide"
                zh-tw-lang="開始上課"
                zh-cn-lang="开始"
                ru-lang="Начало работы"
                ko-lang="시작하기"
                ja-lang="開始する"
                es-lang="Empezar"
                >Get started</a>
            <a href="#" class="next-class-link hide">Next class</a>
          </div>
      </div>
    <?cs /if ?>

  </div> <!-- end jd-content -->

<?cs include:"footer.cs" ?>
</div><!-- end doc-content -->

<?cs include:"trailer.cs" ?>
  <script src="https://developer.android.com/ytblogger_lists_unified.js" type="text/javascript"></script>
  <script src="<?cs var:toroot ?>jd_lists_unified.js?v=15" type="text/javascript"></script>
  <script src="<?cs var:toroot ?>jd_extras.js?v=15" type="text/javascript"></script>
  <script src="<?cs var:toroot ?>jd_collections.js?v=15" type="text/javascript"></script>
  <script src="<?cs var:toroot ?>jd_tag_helpers.js?v=15" type="text/javascript"></script>

</body>
</html>
