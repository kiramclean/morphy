(ns dynamo.metadata-test
  (:require [dynamo.metadata :as sut]
            [clojure.test :refer [deftest testing is]]
            [dynamo.data :as data]
            [datoteka.core :as fs]
            [dynamo.test-utils :as u]))

(def root (str u/resources "metadata/"))

(defn- test-page [file-name]
  (data/->page u/resources (fs/path root file-name)))

(defn- slug [{:keys [path]}]
  (-> path fs/parent fs/name))

(deftest extract-test
  (testing "it pulls front-matter out to the same level as the page"
    (is (= {:my-var "Whatever"
            :a_list ["one" "two" "three"]
            :another/thing "whee"}
           (:front-matter (sut/extract (test-page "lots-of-front-matter.md"))))))

  (testing "it does not leave empty font-matter lying around"
    (is (false?
          (contains? (sut/extract (test-page "custom-slug.md"))
                     :front-matter)))
    (is (false?
          (contains? (sut/extract (test-page "nested/path/custom-slug.md"))
                     :front-matter)))
    (is (false?
          (contains? (sut/extract (test-page "first-line-no-front-matter.md"))
                     :front-matter)))))

(deftest renaming-slug-part-of-paths
  (testing "it defaults the slug to the file name"
    (is (= "lots-of-front-matter"
           (slug (sut/extract (test-page "lots-of-front-matter.md"))))))

  (testing "it's slug is over-writable by user front matter"
    (is (= "custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "custom-slug.md")))))
    (is (= "custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "nested/path/custom-slug.md")))))))

(deftest getting-title
  (testing "it defaults the title to the first line and strips md headers"
    (is (= "This is the first line"
           (:title (sut/extract (test-page "first-line-no-front-matter.md")))))
    (is (= "This is the first line should be the title"
           (:title (sut/extract (test-page "first-line-with-front-matter.md"))))))

  (testing "it's title is over-writable by user front matter"
    (is (= "Custom user title"
           (:title (sut/extract (test-page "title-override.md")))))))
