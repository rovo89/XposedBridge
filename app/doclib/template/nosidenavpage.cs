<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<html<?cs if:devsite ?> devsite<?cs /if ?>>
<?cs include:"head_tag.cs" ?>
<body class="gc-documentation 
  <?cs if:(guide||develop||training||reference||tools||sdk) ?>develop<?cs
  elif:design ?>design<?cs
  elif:distribute ?>distribute<?cs
  /if ?>" itemscope itemtype="http://schema.org/Article">
<a name="top"></a>
<?cs call:custom_masthead() ?>

<div id="body-content">
<div>
<div id="doc-content" style="position:relative;">

<?cs call:tag_list(root.descr) ?>

<?cs include:"footer.cs" ?>
</div><!-- end doc-content -->

<?cs include:"trailer.cs" ?>

</body>
</html>



