import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';

export default function Layout(props) {
  return (
    <>
      <Header />
      <main class="main-content">
        {props.children}
      </main>
      <Footer />
    </>
  );
}
