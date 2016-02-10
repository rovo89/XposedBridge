</div> <!-- end .cols --> <?cs # normally opened by header.cs ?>
</div> <!-- end body-content --> <?cs # normally opened by header.cs ?>

<?cs if:carousel ?>
<script type="text/javascript">
$('.slideshow-container').dacSlideshow({
    btnPrev: '.slideshow-prev',
    btnNext: '.slideshow-next',
    btnPause: '#pauseButton'
});
</script>
<?cs /if ?>
<?cs if:tabbedList ?>
<script type="text/javascript">
$(".feed").dacTabbedList({
    nav_id: '.feed-nav',
    frame_id: '.feed-frame'
});
</script>
<?cs /if ?>

