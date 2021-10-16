package top.itning.yunshunas.music;

public class Test2 {
//    public static List<Music> search(String keyword) throws IOException {
//        Connection.Response response = Jsoup.connect("https://www.kugeci.com/search?q=" + keyword)
//                .header("Host", "www.kugeci.com")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.37")
//                .execute();
//        Elements eachTr = response.parse().body().select("tbody tr");
//        List<Music> list = new ArrayList<>();
//        for (Element element : eachTr) {
//            int i = 0;
//            String name = null;
//            String singer = null;
//            String href = null;
//            for (Element td : element.select("td")) {
//                if (i != 1 && i != 2) {
//                    i++;
//                    continue;
//                }
//                if (i == 1) {
//                    try {
//                        href = td.child(0).attr("href");
//                    } catch (Exception e) {
//                        System.out.println(element);
//                        e.printStackTrace();
//                        //throw e;
//                    }
//                    name = td.wholeText().trim();
//                }
//                if (i == 2) {
//                    singer = td.wholeText().trim();
//                }
//                i++;
//            }
//            Music music = new Music(name, singer, href);
//            list.add(music);
//        }
//        return list;
//    }
//
//    public static void start(List<Need> list) throws Exception {
//        for (Need need : list) {
//            List<Music> searchResult = search(need.name);
//            Optional<Music> first = searchResult.stream().filter(it -> it.getSinger().startsWith(need.getSinger())).findFirst();
//            if (first.isPresent()) {
//                Music music = first.get();
//                String lyric = lyric(music.getHref());
//                if (null != lyric && lyric.length() > 0) {
//                    save(need.musicId, lyric);
//                } else {
//                    System.out.println("歌词获取失败：" + need);
//                }
//            } else {
//                if (searchResult.isEmpty()) {
//                    System.out.println("搜索结果为空：" + need);
//                    continue;
//                }
//                for (int i = 0; i < searchResult.size(); i++) {
//                    System.out.println((i + 1) + "." + searchResult.get(i));
//                }
//                int next = 0;
//                if (next < 1) {
//                    continue;
//                }
//                int index = next - 1;
//                Music music = searchResult.get(index);
//                String lyric = lyric(music.getHref());
//                if (null != lyric && lyric.length() > 0) {
//                    save(need.musicId, lyric);
//                } else {
//                    System.out.println("歌词获取失败：" + need);
//                }
//            }
//        }
//    }
//
//    private static void save(String fileName, String content) throws IOException {
//        FileWriter fileWriter = new FileWriter("E:\\a" + File.separatorChar + fileName);
//        fileWriter.write(content);
//        fileWriter.flush();
//        fileWriter.close();
//    }
//
//    private static String lyric(String href) throws IOException, InterruptedException {
//        Connection.Response response = Jsoup.connect(href)
//                .header("Host", "www.kugeci.com")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.37")
//                .execute();
//        Element element = response.parse().selectFirst(".downloadlrc");
//        HttpResponse<String> re = HttpClient.newHttpClient().send(HttpRequest.newBuilder().GET().uri(URI.create(element.attr("href"))).build(), HttpResponse.BodyHandlers.ofString());
//        return re.body();
//    }
//
//    public static class Need {
//        private String musicId;
//        private String name;
//        private String singer;
//
//        public Need(String musicId, String name, String singer) {
//            this.musicId = musicId;
//            this.name = name;
//            this.singer = singer;
//        }
//
//        public String getMusicId() {
//            return musicId;
//        }
//
//        public void setMusicId(String musicId) {
//            this.musicId = musicId;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getSinger() {
//            return singer;
//        }
//
//        public void setSinger(String singer) {
//            this.singer = singer;
//        }
//
//        @Override
//        public String toString() {
//            return "Need{" +
//                    "musicId='" + musicId + '\'' +
//                    ", name='" + name + '\'' +
//                    ", singer='" + singer + '\'' +
//                    '}';
//        }
//    }
//
//    public static class Music {
//        private String name;
//        private String singer;
//        private String href;
//
//        public Music(String name, String singer, String href) {
//            this.name = name;
//            this.singer = singer;
//            this.href = href;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getSinger() {
//            return singer;
//        }
//
//        public void setSinger(String singer) {
//            this.singer = singer;
//        }
//
//        public String getHref() {
//            return href;
//        }
//
//        public void setHref(String href) {
//            this.href = href;
//        }
//
//        @Override
//        public String toString() {
//            return "Music{" +
//                    "name='" + name + '\'' +
//                    ", singer='" + singer + '\'' +
//                    ", href='" + href + '\'' +
//                    '}';
//        }
//    }
}
