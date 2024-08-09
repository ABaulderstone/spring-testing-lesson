import React, { useEffect, useState } from "react";
import { getAllBlogPosts } from "../../services/blog-services";
import PostCard from "../../components/PostCard/PostCard";

const AllPosts = () => {
  const [fetchStatus, setFetchStatus] = useState("");
  const [posts, setPosts] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    setFetchStatus("LOADING");
    getAllBlogPosts()
      .then((data) => {
        setPosts(data);
        setFetchStatus("SUCCESS");
      })
      .catch((e) => {
        setError(e);
        setFetchStatus("FAILURE");
      });
  }, []);
  return (
    <>
      {fetchStatus === "LOADING" && <p>Loading</p>}
      {fetchStatus === "SUCCESS" &&
        posts.map((post) => <PostCard post={post} />)}
      {fetchStatus === "FAILURE" && <p>{error.message}</p>}
    </>
  );
};

export default AllPosts;
