<?cs # Create a comma separated list of annotations on obj that were in showAnnotations in Doclava ?>
<?cs # pre is an HTML string to start the list, post is an HTML string to close the list ?>
<?cs # for example call:show_annotations_list(cl, "<td>Annotations: ", "</td>") ?>
<?cs # if obj has nothing on obj.showAnnotations, nothing will be output ?>
<?cs def:show_annotations_list(obj) ?>
    <?cs each:anno = obj.showAnnotations ?>
      <?cs if:first(anno) ?>
        <span class='annotation-message'>
          Included in documentation by the annotations:
      <?cs /if ?>
      @<?cs var:anno.type.label ?>
      <?cs if:last(anno) == 0 ?>
        , &nbsp;
      <?cs /if ?>
      <?cs if:last(anno)?>
        </span>
      <?cs /if ?>
    <?cs /each ?>
<?cs /def ?>

<?cs # Override default class_link_table to display annotations ?>
<?cs def:class_link_table(classes) ?>
  <?cs set:count = #1 ?>
  <table class="jd-sumtable-expando">
    <?cs each:cl=classes ?>
      <tr class="<?cs if:count % #2 ?>alt-color<?cs /if ?> api apilevel-<?cs var:cl.type.since ?>" >
        <td class="jd-linkcol"><?cs call:type_link(cl.type) ?></td>
        <td class="jd-descrcol" width="100%">
          <?cs call:short_descr(cl) ?>&nbsp;
          <?cs call:show_annotations_list(cl) ?>
        </td>
      </tr>
      <?cs set:count = count + #1 ?>
    <?cs /each ?>
  </table>
<?cs /def ?>

<?cs # print the API Level ?><?cs
def:since_tags(obj) ?><?cs
if:reference.apilevels && obj.since ?>
  Added in API level <?cs var:obj.since ?><?cs
/if ?>
<?cs /def ?>