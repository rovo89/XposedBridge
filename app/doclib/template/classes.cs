<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<?cs include:"macros_override.cs" ?>
<html<?cs if:devsite ?> devsite<?cs /if ?>>
<?cs include:"head_tag.cs" ?>
<body class="gc-documentation <?cs if:(reference.gms || reference.gcm) ?>google<?cs /if ?>
  <?cs if:(guide||develop||training||reference||tools||sdk) ?>develop<?cs
    if:reference ?> reference<?cs
    /if ?><?cs
  elif:design ?>design<?cs
  elif:distribute ?>distribute<?cs
  /if ?>" itemscope itemtype="http://schema.org/Article">
  <a name="top"></a>
<?cs include:"header.cs" ?>

<div class="col-12" id="doc-col">

<div id="jd-header">
<h1><?cs var:page.title ?></h1>
</div>

<div id="jd-content">
<p>These are the Xposed Framework API classes. See all <a href="packages.html">API packages</a>.</p>
<div class="jd-letterlist"><?cs each:letter=docs.classes ?>
    <a href="#letter_<?cs name:letter ?>"><?cs name:letter ?></a>&nbsp;&nbsp;<?cs /each?>
</div>

<?cs each:letter=docs.classes ?>
<?cs set:count = #1 ?>
<h2 id="letter_<?cs name:letter ?>"><?cs name:letter ?></h2>
<table class="jd-sumtable">
    <?cs set:cur_row = #0 ?>
    <?cs each:cl = letter ?>
        <tr class="<?cs if:count % #2 ?>alt-color<?cs /if ?> api apilevel-<?cs var:cl.since ?>" >
            <td class="jd-linkcol"><?cs call:type_link(cl.type) ?></td>
            <td class="jd-descrcol" width="100%">
              <?cs call:short_descr(cl) ?>&nbsp;
              <?cs call:show_annotations_list(cl) ?>
            </td>
        </tr>
    <?cs set:count = count + #1 ?>
    <?cs /each ?>
</table>
<?cs /each ?>

</div><!-- end jd-content -->

<?cs include:"footer.cs" ?>
</div><!-- end doc-content -->

<?cs include:"trailer.cs" ?>

</body>
</html>
