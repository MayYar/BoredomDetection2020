package labelingStudy.nctu.minuku_2;

public class PostNews {

    public String title;
    public String content;
    public String URL;

    public PostNews() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public PostNews(String title, String content, String URL) {
        this.title= title;
        this.content = content;
        this.URL = URL;
    }

}
