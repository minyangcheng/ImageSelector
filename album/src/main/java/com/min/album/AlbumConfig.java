package com.min.album;

/**
 * Created by minyangcheng on 2016/11/23.
 */

public class AlbumConfig {

    private AlbumImageLoader mImageLoader;
    private int columnNum;

    public AlbumConfig(Builder builder){
        this.mImageLoader=builder.imageLoader;
        this.columnNum=builder.columnNum;
    }

    public AlbumImageLoader getImageLoader() {
        return this.mImageLoader;
    }

    public int getColumnNum(){
        return this.columnNum;
    }

    public static class Builder{

        public AlbumImageLoader imageLoader;
        public int columnNum;

        public AlbumConfig build(){
            initEmptyField();
            return new AlbumConfig(this);
        }

        private void initEmptyField() {
            if(columnNum<=0){
                this.columnNum=2;
            }
        }

        public Builder setImageLoader(AlbumImageLoader imageLoader){
            this.imageLoader=imageLoader;
            return this;
        }

        public Builder setColumnNum(int columnNum){
            this.columnNum=columnNum;
            return this;
        }

    }

}
