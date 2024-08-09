import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
dayjs.extend(relativeTime);

const PostCard = ({ post }) => {
  //   console.log(relativeTime);
  console.log(dayjs);
  return (
    <div>
      <h2>{post.title}</h2>
      <h4>posted {dayjs(post.createdAt).fromNow()}</h4>
      <p>{post.content}</p>
    </div>
  );
};

export default PostCard;
